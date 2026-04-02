package org.project.pet_health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "评论及多级回复的数据传输对象")
public class CommentDTO {

    @Schema(description = "评论ID")
    private Long commentId;

    @Schema(description = "关联的动态/帖子ID")
    private Long postId;

    @Schema(description = "发表评论的用户ID")
    private Long userId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父级评论ID (一级评论为0或null)")
    private Long parentId;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;

    // ================= 发送者（评论人）信息 =================
    @Schema(description = "评论人昵称")
    private String nickname;

    @Schema(description = "评论人头像")
    private String avatar;

    // ================= 目标（被回复人）信息 =================
    // 💡 这两个字段是实现“回复的回复”的关键！
    @Schema(description = "被回复的用户ID (如果是顶级评论则为null)")
    private Long replyToUserId;

    @Schema(description = "被回复的用户昵称 (用于前端展示 '@某人')")
    private String replyToNickname;

    // ================= 子评论（回复列表） =================
    @Schema(description = "该评论下的所有子回复列表")
    private List<CommentDTO> replies;
}
