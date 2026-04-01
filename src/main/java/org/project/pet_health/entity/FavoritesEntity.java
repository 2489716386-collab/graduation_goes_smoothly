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
 * 用户收藏表
 * </p>
 *
 * @author weiling
 * @since 2026-04-01
 */
@Getter
@Setter
@ToString
@TableName("favorites")
@Tag(name = "FavoritesEntity对象", description = "用户收藏表")
public class FavoritesEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 收藏者的用户ID
     */
    @TableField("user_id")
    @Schema(description = "收藏者的用户ID")
    private Long userId;

    /**
     * 被收藏的社区动态ID
     */
    @TableField("post_id")
    @Schema(description = "被收藏的社区动态ID")
    private Long postId;

    /**
     * 收藏时间
     */
    @Schema(description = "收藏时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
