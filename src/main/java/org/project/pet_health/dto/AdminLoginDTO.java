package org.project.pet_health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AdminLoginDTO {

    @Schema(description = "管理员账号/用户名")
    private String username;

    @Schema(description = "管理员密码")
    private String password;
}
