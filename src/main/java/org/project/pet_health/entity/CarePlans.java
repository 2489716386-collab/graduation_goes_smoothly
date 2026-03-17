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
@TableName("care_plans")
@Tag(name = "养护计划", description = "")
public class CarePlans implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "plan_id", type = IdType.AUTO)
    private Long planId;

    @TableField("pet_id")
    private Long petId;

    @Schema(description = "饮食方案描述")
    @TableField("diet_plan")
    private String dietPlan;

    @Schema(description = "每日运动时长")
    @TableField("exercise_duration")
    private Integer exerciseDuration;

    @Schema(description = "每周频次")
    @TableField("exercise_frequency")
    private Integer exerciseFrequency;

    @TableField(value = "update_time",fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
}
