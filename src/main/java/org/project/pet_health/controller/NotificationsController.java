package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.entity.Notifications;
import org.project.pet_health.enums.NotificationType;
import org.project.pet_health.service.NotificationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@Tag(name = "PC后台-系统通知管理")
public class NotificationsController {

    @Autowired
    private NotificationsService notificationsService;

    @GetMapping("/admin/page")
    @Operation(summary = "分页条件查询系统通知")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer noticeId,
                       @RequestParam(required = false) String content,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) Integer type) {
        NotificationType queryType = null;
        if (type != null) {
            for (NotificationType t : NotificationType.values()) {
                if (t.name().equals(type)) {
                    queryType = t;
                    break;
                }
            }
        }
        return Result.success(notificationsService.getAdminPage(pageNum, pageSize, content, startDate, endDate, type,noticeId));
    }

    @PostMapping("/admin/add")
    @Operation(summary = "新增系统通知")
    @LogAction("发送了系统通知: #{#notification.title}") // AOP自动记录日志
    public Result add(@RequestBody Notifications notification) {
        // 前端不传 is_read 字段，数据库就不会管它，符合你后续打算废弃它的计划
        notificationsService.save(notification);
        return Result.success("系统通知发送成功！");
    }
}
