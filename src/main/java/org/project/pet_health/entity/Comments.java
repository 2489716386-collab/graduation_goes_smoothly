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
 * 社区动态评论表
 * </p>
 *
 * @author weiling
 * @since 2026-03-19
 */
@Getter
@Setter
@ToString
@TableName("comments")
@Tag(name = "CommentsEntity对象", description = "社区动态评论表")
public class Comments implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID（主键）
     */
    @Schema(description = "评论ID（主键）")
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 关联的动态ID（外键，关联community_posts的post_id）
     */
    @TableField("post_id")
    @Schema(description = "关联的动态ID（外键，关联community_posts的post_id）")
    private Long postId;

    /**
     * 发布评论的用户ID（外键，关联用户表user_id）
     */
    @TableField("user_id")
    @Schema(description = "发布评论的用户ID（外键，关联用户表user_id）")
    private Long userId;

    /**
     * 评论文字内容
     */
    @TableField("content")
    @Schema(description = "评论文字内容")
    private String content;

    /**
     * 父评论ID（0表示一级评论，非0表示回复）
     */
    @TableField("parent_id")
    @Schema(description = "父评论ID（0表示一级评论，非0表示回复）")
    private Long parentId;

    /**
     * 评论点赞数
     */
    @TableField("like_count")
    @Schema(description = "点赞数")
    private Integer likeCount;

    @TableField("comment_count")
    @Schema(description = "评论数")
    private Integer CommentCount;

    /**
     * 审核状态：0-待审核，1-已发布，2-违规拦截
     */
    @TableField("status")
    @Schema(description = "审核状态：0-待审核，1-已发布，2-违规拦截")
    private Integer status;

    /**
     * 评论发布时间
     */
    @TableField("create_time")
    @Schema(description = "评论发布时间")
    private LocalDateTime createTime;

    @Schema(description = "被举报次数")
    @TableField("report_count")
    private Integer ReportCount;

    /**
     * 评论更新时间（比如修改内容）
     */
    @TableField("update_time")
    @Schema(description = "评论更新时间（比如修改内容）")
    private LocalDateTime updateTime;
}
