package org.project.pet_health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserBanDTO {

    @Schema(description = "被封禁的用户ID")
    private Long userId;

    @Schema(description = "封禁天数 (例如: 14代表两周, 30代表一个月, 90代表三个月, 180代表半年, 365代表一年)")
    private Integer banDays;

    @Schema(description = "封禁原因")
    private String reason;
}
