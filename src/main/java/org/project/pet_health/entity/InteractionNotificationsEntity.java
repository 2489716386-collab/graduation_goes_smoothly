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
 * 用户互动通知表
 * </p>
 *
 * @author weiling
 * @since 2026-04-01
 */
@Getter
@Setter
@ToString
@TableName("interaction_notifications")
@Tag(name =  "InteractionNotificationsEntity对象", description = "用户互动通知表")
public class InteractionNotificationsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description ="主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收者(帖子作者)ID
     */
    @TableField("receiver_id")
    @Schema(description ="接收者(帖子作者)ID")
    private Long receiverId;

    /**
     * 发送者(操作人)ID
     */
    @TableField("sender_id")
    @Schema(description ="发送者(操作人)ID")
    private Long senderId;

    /**
     * 通知类型: LIKE(点赞), COMMENT(评论), FAVORITE(收藏)
     */
    @TableField("type")
    @Schema(description ="通知类型: LIKE(点赞), COMMENT(评论), FAVORITE(收藏)")
    private String type;

    /**
     * 关联的动态/帖子ID
     */
    @TableField("post_id")
    @Schema(description ="关联的动态/帖子ID")
    private Long postId;

    /**
     * 附加内容（如评论详情）
     */
    @TableField("content")
    @Schema(description ="附加内容（如评论详情）")
    private String content;

    /**
     * 阅读状态: 0-未读, 1-已读
     */
    @TableField("is_read")
    @Schema(description ="阅读状态: 0-未读, 1-已读")
    private Boolean isRead;

    /**
     * 触发时间
     */
    @Schema(description ="触发时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
