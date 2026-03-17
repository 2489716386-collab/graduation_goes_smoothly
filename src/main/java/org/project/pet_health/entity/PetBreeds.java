package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

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
@TableName("pet_breeds")
@Tag(name = " 宠物品种词典表", description = "")
public class PetBreeds implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "breed_id", type = IdType.AUTO)
    private Integer breedId;

    @Schema(description = "大类：猫、狗、兔等")
    @TableField("species_type")
    private String speciesType;

    @Schema(description = "品种名称：如布偶猫")
    @TableField("breed_name")
    private String breedName;

    @Schema(description = "首字母检索")
    @TableField("initial")
    private String initial;
}
