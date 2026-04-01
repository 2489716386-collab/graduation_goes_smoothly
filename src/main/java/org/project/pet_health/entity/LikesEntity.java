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
 * 用户点赞表
 * </p>
 *
 * @author weiling
 * @since 2026-04-01
 */
@Getter
@Setter
@ToString
@TableName("likes")
@Tag(name = "LikesEntity对象", description = "用户点赞表")
public class LikesEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(value = "like_id", type = IdType.AUTO)
    private Long likeId;

    /**
     * 点赞用户的ID
     */
    @TableField("user_id")
    @Schema(description = "点赞用户的ID")
    private Long userId;

    /**
     * 被点赞动态的ID
     */
    @TableField("post_id")
    @Schema(description = "被点赞动态的ID")
    private Long postId;

    /**
     * 点赞时间
     */
    @Schema(description = "点赞时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
