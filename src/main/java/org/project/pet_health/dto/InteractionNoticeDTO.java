package org.project.pet_health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "互动通知列表返回数据")
public class InteractionNoticeDTO {

    @Schema(description = "通知的主键ID")
    private Long id;

    @Schema(description = "通知类型: LIKE, COMMENT, FAVORITE")
    private String type;

    @Schema(description = "前端直接展示的动作文案 (例如：赞了你的动态)")
    private String actionText;

    @Schema(description = "关联的动态/帖子ID，用于点击跳转")
    private Long postId;

    @Schema(description = "是否已读 (0-未读, 1-已读)")
    private Integer isRead;

    @Schema(description = "通知产生的时间")
    private LocalDateTime createTime;

    // ================= 发送者（操作人）信息 =================
    @Schema(description = "操作人的用户ID")
    private Long senderId;

    @Schema(description = "操作人的昵称")
    private String senderNickname;

    @Schema(description = "操作人的头像URL")
    private String senderAvatar;

    // ================= 被操作的帖子信息 =================
    @Schema(description = "被点赞/评论的帖子内容摘要（截取前15个字）")
    private String postSummary;

    private Long notificationId;

    private String postContentTeaser; // 动态内容缩略（前20字）
    private String content;           // 如果是评论，这里存评论内容
}
