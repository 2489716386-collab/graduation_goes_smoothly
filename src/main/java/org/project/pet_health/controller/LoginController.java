package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.dto.AdminLoginDTO;
import org.project.pet_health.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth") // 专门的认证路径
@Tag(name = "统一登录鉴权中心")
public class LoginController {

    @Autowired
    private UsersService usersService;

    @PostMapping("/wx-login")
    @Operation(summary = "【小程序】微信快捷登录")
    public Result wxLogin(@RequestBody Map<String, String> loginForm) {
        String code = loginForm.get("code");
        if (code == null) {
            return Result.error("Code不能为空");
        }

        // 调用刚刚补全的 Service 方法，只需传 code
        String token = usersService.wxLogin(code);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

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
