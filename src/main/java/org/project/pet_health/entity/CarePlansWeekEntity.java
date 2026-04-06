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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * AI养护周计划主表
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
@Getter
@Setter
@ToString
@TableName("care_plans_week")
@Tag(name ="CarePlansWeekEntity对象", description = "AI养护周计划主表")
public class CarePlansWeekEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description ="主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description ="用户ID")
    private Long userId;

    /**
     * 宠物ID
     */
    @TableField("pet_id")
    @Schema(description ="宠物ID")
    private Long petId;

    /**
     * 生成时的年龄(如: 3岁2个月)
     */
    @TableField("snapshot_age")
    @Schema(description ="生成时的年龄(如: 3岁2个月)")
    private String snapshotAge;

    /**
     * 生成时的体重(kg)
     */
    @TableField("snapshot_weight")
    @Schema(description ="生成时的体重(kg)")
    private BigDecimal snapshotWeight;

    /**
     * 当时的健康状况(用户填写或OCR提取)
     */
    @TableField("snapshot_health_status")
    @Schema(description ="当时的健康状况(用户填写或OCR提取)")
    private String snapshotHealthStatus;

    /**
     * AI生成的本周养护重点
     */
    @TableField("weekly_focus")
    @Schema(description ="AI生成的本周养护重点")
    private String weeklyFocus;

    /**
     * 本周计划开始日期
     */
    @TableField("start_date")
    @Schema(description ="本周计划开始日期")
    private LocalDate startDate;

    /**
     * 本周计划结束日期
     */
    @TableField("end_date")
    @Schema(description ="本周计划结束日期")
    private LocalDate endDate;

    /**
     * 是否为当前生效计划(1生效, 0历史记录)
     */
    @TableField("is_current")
    @Schema(description ="是否为当前生效计划(1生效, 0历史记录)")
    private Boolean isCurrent;

    /**
     * 生成时间
     */
    @Schema(description ="生成时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
