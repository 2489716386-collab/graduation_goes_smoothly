package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.dto.AdminLoginDTO;
import org.project.pet_health.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth") // 专门的认证路径
@Tag(name = "统一登录鉴权中心")
public class LoginController {

    @Autowired
    private UsersService usersService;

//    @PostMapping("/wx-login")
//    @Operation(summary = "【小程序】微信快捷登录")
//    public Result wxLogin(@RequestBody Map<String, String> loginForm) {
//        String code = loginForm.get("code");
//        String nickname = loginForm.get("nickname");
//        String avatarUrl = loginForm.get("avatarUrl");
//
//        // 调用 Service 走注册/登录逻辑，拿到 Token
//        String token = usersService.wxLogin(code, nickname, avatarUrl);
//        return Result.success(Map.of("token", token));
//    }

    @PostMapping("/admin-login")
    @Operation(summary = "【PC后台】管理员账号密码登录")
    public Result adminLogin(@RequestBody AdminLoginDTO loginDTO) {
        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            return Result.error("账号或密码不能为空");
        }

        // 调用 Service 进行登录校验，成功则返回 Token
        String token = usersService.adminLogin(loginDTO);
        return Result.success(Map.of("token", token));
    }
}
