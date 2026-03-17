package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author weiling
 * @since 2026-03-11
 */
@Getter
@Setter
@TableName("user_blacklist")
@Tag(name = "用户黑名单表", description = "")
public class UserBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "black_id", type = IdType.AUTO)
    private Long blackId;

    @Schema(description = "被封禁用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "封禁原因")
    @TableField("reason")
    private String reason;

    @Schema(description = "过期时间")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
}
