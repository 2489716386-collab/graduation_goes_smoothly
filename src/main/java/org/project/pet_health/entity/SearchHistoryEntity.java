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
 * 用户搜索历史表
 * </p>
 *
 * @author weiling
 * @since 2026-04-01
 */
@Getter
@Setter
@ToString
@TableName("search_history")
@Tag(name = "SearchHistoryEntity对象", description = "用户搜索历史表")
public class SearchHistoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 搜索者的用户ID
     */
    @TableField("user_id")
    @Schema(description = "搜索者的用户ID")
    private Long userId;

    /**
     * 搜索关键词
     */
    @TableField("keyword")
    @Schema(description = "搜索关键词")
    private String keyword;

    /**
     * 搜索时间
     */
    @Schema(description = "搜索时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
