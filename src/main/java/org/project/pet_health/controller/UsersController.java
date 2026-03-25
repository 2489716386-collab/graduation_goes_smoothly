package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.dto.UserBanDTO;
import org.project.pet_health.entity.Users;
import org.project.pet_health.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "用户管理")
public class UsersController {

    @Autowired
    private UsersService usersService;

    @GetMapping("/admin/page")
    @Operation(summary = "【PC】分页条件查询所有用户")
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) String nickname,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) Long userId) {
        return Result.success(usersService.getAdminUsersPage(pageNum, pageSize, nickname, userId, role));
    }

    @PostMapping("/admin/unban")
    @Operation(summary = "【PC】封禁用户并加入黑名单")
    @LogAction("封禁了用户，用户ID: #{#banDTO.userId}")
    public Result banUser(@RequestBody UserBanDTO banDTO) {
        usersService.banUser(banDTO);
        return Result.success("该用户已被成功封禁！");
    }

    // ... 原有的 admin 接口下方 ...

    @GetMapping("/user/profile")
    @Operation(summary = "【小程序】获取当前登录用户信息")
    public Result getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(usersService.getUserProfile(userId));
    }

    @PutMapping("/user/update")
    @Operation(summary = "【小程序】修改个人资料(昵称、头像)")
    public Result updateProfile(@RequestBody Users user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        usersService.updateUserProfile(user, userId);
        return Result.success("资料修改成功");
    }
}
