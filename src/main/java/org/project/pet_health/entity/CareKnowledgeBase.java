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
@TableName("care_knowledge_base")
@Tag(name = "CareKnowledgeBase对象", description = "")
public class CareKnowledgeBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "品种")
    @TableField("species")
    private String species;

    @Schema(description = "幼年/成年/老年")
    @TableField("life_stage")
    private String lifeStage;

    @Schema(description = "每公斤热量需求")
    @TableField("kcal_per_kg")
    private Double kcalPerKg;

    @Schema(description = "建议运动时长(分)")
    @TableField("suggest_exercise")
    private Integer suggestExercise;

    @Schema(description = "注意事项")
    @TableField("caution")
    private String caution;
}
