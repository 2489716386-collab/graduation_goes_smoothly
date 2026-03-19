package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.dto.UserBanDTO;
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
    @Operation(summary = "【后台】分页条件查询所有用户")
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) String nickname,
                            @RequestParam(required = false) Long userId) {
        return Result.success(usersService.getAdminUsersPage(pageNum, pageSize, nickname, userId));
    }

    @PostMapping("/admin/ban")
    @Operation(summary = "【后台】封禁用户并加入黑名单")
    @LogAction("封禁了用户，用户ID: #{#banDTO.userId}")
    public Result banUser(@RequestBody UserBanDTO banDTO) {
        usersService.banUser(banDTO);
        return Result.success("该用户已被成功封禁！");
    }
}
