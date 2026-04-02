package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.Result;
import org.project.pet_health.service.FavoritesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
@Tag(name = "帖子收藏模块")
public class FavoritesController {

    @Autowired
    private FavoritesService favoritesService;

    @PostMapping("/toggle/{postId}")
    @Operation(summary = "切换收藏/取消收藏")
    public Result toggle(@PathVariable Long postId, HttpServletRequest request) {
        // 从拦截器中获取当前用户ID
        Long userId = (Long) request.getAttribute("currentUserId");
        boolean isFavorited = favoritesService.toggleFavorite(postId, userId);

        // 遵循 Result.success 只有一个参数的规范，返回布尔值状态
        return Result.success(isFavorited);
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前用户的收藏列表")
    public Result getMyFavorites(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(favoritesService.getMyFavorites(pageNum, pageSize, userId));
    }
}
