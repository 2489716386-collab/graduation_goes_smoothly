package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.project.pet_health.enums.AuditStatus;

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
@TableName("community_posts")
@Tag(name = "社区动态表", description = "")
public class CommunityPosts implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    @TableField("user_id")
    private Long userId;

    @Schema(description = "文字内容")
    @TableField("content")
    private String content;

    @Schema(description = "多图地址(JSON序列化)")
    @TableField("media_urls")
    private String mediaUrls;

    @Schema(description = "0普通, 1心情分享")
    @TableField("post_type")
    private Integer postType;

    @Schema(description = "喜欢次数")
    @TableField("like_count")
    private Integer likeCount;

    @Schema(description = "审核状态: 0审核中, 1已发布, 2拦截")
    @TableField("status")
    private AuditStatus status;

    @Schema(description = "被举报次数")
    @TableField("report_count")
    private Integer ReportCount;

    @Schema(description = "评论次数")
    @TableField("comment_count")
    private Integer CommentCount;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;
}
