package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 每日养护打卡任务表
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
@Getter
@Setter
@ToString
@TableName("care_plans_day")
@Tag(name = "CarePlansDayEntity对象", description = "每日养护打卡任务表")
public class CarePlansDayEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description ="主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的周计划ID
     */
    @TableField("week_plan_id")
    @Schema(description ="关联的周计划ID")
    private Long weekPlanId;

    /**
     * 冗余宠物ID(方便直接查询某宠物的今日任务)
     */
    @TableField("pet_id")
    @Schema(description ="冗余宠物ID(方便直接查询某宠物的今日任务)")
    private Long petId;

    /**
     * 具体执行日期(如: 2026-04-15)
     */
    @TableField("plan_date")
    @Schema(description ="具体执行日期(如: 2026-04-15)")
    private LocalDate planDate;

    /**
     * 星期几(1-7，方便前端按星期分类展示)
     */
    @TableField("day_of_week")
    @Schema(description ="星期几(1-7，方便前端按星期分类展示)")
    private Integer dayOfWeek;

    /**
     * 任务分类(饮食、运动、清洁、医疗等)
     */
    @TableField("task_category")
    @Schema(description ="任务分类(饮食、运动、清洁、医疗等)")
    private String taskCategory;

    /**
     * 具体任务内容
     */
    @Schema(description ="具体任务内容")
    @TableField("task_content")
    private String taskContent;

    /**
     * 打卡状态(0未完成, 1已完成)
     */
    @TableField("is_completed")
    @Schema(description ="打卡进度")
    private Integer isCompleted;
}
