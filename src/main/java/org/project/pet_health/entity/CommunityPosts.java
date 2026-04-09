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
    private Integer reportCount;

    @Schema(description = "评论次数")
    @TableField("comment_count")
    private Integer commentCount;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("content_vector")
    @Schema(description = "AI语义向量数据")
    private String contentVector; // 数据库存储 JSON 格式的数组字符串

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;


    // 👇👇👇 补上这两个极其重要的状态字段 👇👇👇
    @Schema(description = "当前用户是否已点赞(前端展示用)")
    @TableField(exist = false)
    private Boolean isLiked;

    @Schema(description = "当前用户是否已收藏(前端展示用)")
    @TableField(exist = false)
    private Boolean isFavorited;

    // =========== 以下为非数据库字段，仅供业务传输使用 ===========

    @Schema(description = "算法推荐得分(非数据库字段)")
    @TableField(exist = false) // 极其重要：告诉 MyBatisPlus 数据库里没这个列，插入更新时忽略它
    private Integer recommendScore;



    @TableField(exist = false)
    private Double similarityScore; // 临时字段：存储计算出的相似度得分
}
