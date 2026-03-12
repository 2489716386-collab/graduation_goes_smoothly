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
@TableName("community_posts")
@Tag(name = "CommunityPosts对象", description = "")
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
    private Boolean postType;

    @TableField("like_count")
    private Integer likeCount;

    @Schema(description = "审核状态: 0审核中, 1已发布, 2拦截")
    @TableField("status")
    private Boolean status;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
