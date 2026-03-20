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
import java.time.LocalDateTime;

/**
 * <p>
 * 用户举报记录表
 * </p>
 *
 * @author weiling
 * @since 2026-03-20
 */
@Getter
@Setter
@ToString
@TableName("reports")
@Tag(name = "ReportsEntity对象", description = "用户举报记录表")
public class ReportsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 举报ID（主键）
     */
    @Schema(description ="举报ID（主键）")
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    /**
     * 举报人用户ID（外键，关联用户表user_id）
     */
    @TableField("reporter_id")
    @Schema(description ="举报人用户ID（外键，关联用户表user_id）")
    private Long reporterId;

    /**
     * 被举报的主体ID（动态ID/评论ID）
     */
    @TableField("target_id")
    @Schema(description ="被举报的主体ID（动态ID/评论ID）")
    private Long targetId;

    /**
     * 举报类型：post-动态，comment-评论
     */
    @TableField("target_type")
    @Schema(description ="举报类型：post-动态，comment-评论")
    private String targetType;

    /**
     * 举报理由（违规、低俗、虚假信息等）
     */
    @TableField("reason")
    @Schema(description ="举报理由（违规、低俗、虚假信息等）")
    private String reason;

    /**
     * 处理状态：0-待处理，1-已处理-忽略，2-已处理-删除内容
     */
    @TableField("status")
    @Schema(description ="处理状态：0-待处理，1-已处理-忽略，2-已处理-删除内容")
    private Boolean status;

    /**
     * 举报时间
     */
    @Schema(description ="举报时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
