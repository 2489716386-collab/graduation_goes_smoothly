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
@TableName("health_records")
@Tag(name = "HealthRecords对象", description = "")
public class HealthRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @TableField("pet_id")
    private Long petId;

    @Schema(description = "体检日期")
    @TableField("checkup_date")
    private LocalDate checkupDate;

    @Schema(description = "报告图片地址(JSON序列化)")
    @TableField("report_files")
    private String reportFiles;

    @Schema(description = "健康评分")
    @TableField("health_score")
    private Integer healthScore;

    @Schema(description = "异常备注")
    @TableField("description")
    private String description;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
}
