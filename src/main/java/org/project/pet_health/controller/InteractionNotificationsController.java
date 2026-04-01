package org.project.pet_health.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.dto.InteractionNoticeDTO;
import org.project.pet_health.service.InteractionNotificationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/interaction-notices")
@Tag(name = "互动通知中心")
public class InteractionNotificationsController {

    @Autowired
    private InteractionNotificationsService noticeService;

    @GetMapping("/unread/count")
    @Operation(summary = "查询未读互动通知数量")
    public Result<Long> getCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(noticeService.getUnreadCount(userId));
    }

    @GetMapping("/list")
    @Operation(summary = "获取互动通知列表")
    public Result<Page<InteractionNoticeDTO>> getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(noticeService.getNoticeList(userId, pageNum, pageSize));
    }

    @PutMapping("/read-all")
    @Operation(summary = "一键标记所有通知为已读")
    public Result markRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        noticeService.markAllAsRead(userId);
        return Result.success();
    }
}
