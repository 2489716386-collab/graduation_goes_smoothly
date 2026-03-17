package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("comments")
@Tag(name = "动态互动（评论）", description = "")
public class Comments implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    @TableField("post_id")
    private Long postId;

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
