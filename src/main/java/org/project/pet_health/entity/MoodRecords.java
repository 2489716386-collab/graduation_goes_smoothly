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
 * 宠物心情与日记记录表
 * </p>
 *
 * @author weiling
 * @since 2026-04-07
 */
@Getter
@Setter
@TableName("mood_records")
@Tag(name = "宠物心情记录表", description = "用于存储AI情绪识别结果及生成的心情日志")
public class MoodRecords implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "mood_id", type = IdType.AUTO)
    private Long moodId;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "宠物ID")
    @TableField("pet_id")
    private Long petId;

    @Schema(description = "日记归属日期")
    @TableField("record_date")
    private LocalDate recordDate;

    @Schema(description = "宠物心情照片地址(通常在uniCloud或对象存储中)")
    @TableField("image_url")
    private String imageUrl;

    @Schema(description = "识别出的情绪结果(如: happy, sad)")
    @TableField("mood_type")
    private String moodType;

    @Schema(description = "AI情绪分类置信度")
    @TableField("confidence")
    private Double confidence;

    @Schema(description = "大语言模型(DeepSeek)生成的日记内容")
    @TableField("diary_content")
    private String diaryContent;

    @Schema(description = "是否已分享到社区: 0-未分享, 1-已分享")
    @TableField("is_shared")
    private Integer isShared;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除标识: 0-正常, 1-已删除")
    @TableLogic // MyBatis-Plus 逻辑删除专属注解
    @TableField("is_deleted")
    private Integer isDeleted;
}
