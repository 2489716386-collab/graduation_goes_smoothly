package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.project.pet_health.enums.StatusType;

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
@TableName("users")
@Tag(name = "Users对象", description = "")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户唯一ID")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @Schema(description = "微信唯一标识")
    @TableField("openid")
    private String openid;

    @Schema(description = "登录名(管理员用)")
    @TableField("username")
    private String username;

    @Schema(description = "密码(管理员用)")
    @TableField("password")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Length(max = 20,message = "昵称长度不能超过30个字！")
    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "头像地址")
    @TableField("avatar_url")
    private String avatarUrl;

    @Schema(description = "角色: user普通用户, admin管理员")
    @TableField("role")
    private String role;

    @Schema(description = "状态: 1正常, 0封禁")
    @TableField("status")
    private StatusType status;

    @Schema(description = "注册时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
}
