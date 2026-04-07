package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.entity.Notifications;
import org.project.pet_health.entity.Users;
import org.project.pet_health.enums.NotificationType;
import org.project.pet_health.service.NotificationsService;
import org.project.pet_health.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Tag(name = "PC后台-系统通知管理")
public class NotificationsController {

    @Autowired
    private NotificationsService notificationsService;

    @Autowired
    private UsersService usersService;

    @GetMapping("/admin/page")
    @Operation(summary = "分页条件查询系统通知")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "20") Integer pageSize,
                       @RequestParam(required = false) Integer noticeId,
                       @RequestParam(required = false) String content,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) Integer type) {
        NotificationType queryType = null;
        if (type != null) {
            for (NotificationType t : NotificationType.values()) {
                if (t.getValue().equals(type)) {
                    queryType = t;
                    break;
                }
            }
        }
        return Result.success(notificationsService.getAdminPage(pageNum, pageSize, content, startDate, endDate, queryType,noticeId));
    }

    @PostMapping("/admin/add")
    @Operation(summary = "新增系统通知")
    @LogAction("发送了系统通知: #{#notification.title}") // AOP自动记录日志
    public Result add(@RequestBody Notifications notification) {
        // 前端不传 is_read 字段，数据库就不会管它，符合你后续打算废弃它的计划
        notificationsService.save(notification);
        return Result.success("系统通知发送成功！");
    }

    @GetMapping("/user/list")
    @Operation(summary = "小程序端-获取当前用户的系统通知")
    public Result getUserNotices(@RequestHeader(name = "token") String token) {
        try {
            // Controller 现在非常干净，只负责一件事：呼叫 Service 干活，然后包装成 Result 返回
            return Result.success(notificationsService.getUserNoticesByToken(token));

        } catch (Exception e) {
            // 捕获 Service 层抛出的异常（比如 Token 解析失败），友好地返回给前端
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/unread-count")
    @Operation(summary = "获取未读全局通知数量")
    public Result getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");

        // 1. 查出当前用户的 last_read_notice_id
        Users user = usersService.getById(userId);
        Long lastReadId = user.getLastReadNoticeId() != null ? user.getLastReadNoticeId() : 0L;

        // 2. 查出通知表里，id 大于这个 lastReadId 的数量
        // SQL: SELECT count(*) FROM notifications WHERE notice_id > #{lastReadId} AND target_type = 'ALL'
        long count = notificationsService.count(
                new LambdaQueryWrapper<Notifications>()
                        .gt(Notifications::getNoticeId, lastReadId)
                // 如果你有 targetType 区分全局和个人，加上这句
                // .eq(Notifications::getTargetType, "ALL")
        );

        return Result.success(count);
    }

    @PostMapping("/user/mark-read")
    @Operation(summary = "标记所有系统通知为已读")
    public Result markNoticesRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");

        // 查出现在系统中最新的一条全局通知的 ID
        Notifications latestNotice = notificationsService.getOne(
                new LambdaQueryWrapper<Notifications>()
                        .orderByDesc(Notifications::getNoticeId)
                        .last("LIMIT 1")
        );

        if (latestNotice != null) {
            // 更新用户的 last_read_notice_id
            Users updateEntity = new Users();
            updateEntity.setUserId(userId);
            updateEntity.setLastReadNoticeId(latestNotice.getNoticeId());
            usersService.updateById(updateEntity);
        }

        return Result.success("已读状态已更新");
    }
}
