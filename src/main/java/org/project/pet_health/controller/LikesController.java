package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.Result;
import org.project.pet_health.service.LikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户点赞表 前端控制器
 * </p>
 *
 * @author weiling
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/likes")
public class LikesController {
    @Autowired
    private LikesService likesService;

    @PostMapping("/toggle/{postId}")
    @Operation(summary = "点赞/取消点赞切换")
    public Result toggle(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        boolean isLiked = likesService.toggleLike(postId, userId);
        return Result.success(isLiked);
    }

    @GetMapping("/my")
    @Operation(summary = "获取我喜欢的动态列表")
    public Result getMyLikes(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(likesService.getMyLikedPosts(userId, pageNum, pageSize));
    }
}
