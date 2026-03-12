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
@TableName("mood_records")
@Tag(name = "MoodRecords对象", description = "")
public class MoodRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "mood_id", type = IdType.AUTO)
    private Long moodId;

    @TableField("pet_id")
    private Long petId;

    @Schema(description = "识别原图地址")
    @TableField("image_url")
    private String imageUrl;

    @Schema(description = "识别出的情绪结果")
    @TableField("mood_type")
    private String moodType;

    @Schema(description = "YOLO置信度")
    @TableField("confidence")
    private Double confidence;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
