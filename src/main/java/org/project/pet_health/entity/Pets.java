package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
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
@TableName("pets")
@Tag(name = "Pets对象", description = "")
public class Pets implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "pet_id", type = IdType.AUTO)
    private Long petId;

    @Schema(description = "所属用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "宠物名字")
    @TableField("name")
    private String name;

    @Schema(description = "品种ID关联")
    @TableField("breed_id")
    private Integer breedId;

    @Schema(description = "性别: 0未知, 1公, 2母")
    @TableField("gender")
    private Boolean gender;

    @Schema(description = "出生日期")
    @TableField("birth_date")
    private LocalDate birthDate;

    @Schema(description = "当前体重(kg)")
    @TableField("weight")
    private Double weight;

    @Schema(description = "宠物头像")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "是否绝育")
    @TableField("is_neutered")
    private Boolean isNeutered;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
