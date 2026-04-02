package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.dto.CommentDTO;
import org.project.pet_health.entity.Comments;
import org.project.pet_health.enums.AuditStatus;

import java.util.List;

public interface CommentsService extends IService<Comments> {
    Page<Comments> getAdminPage(Integer pageNum, Integer pageSize, AuditStatus status, String content, String startDate, String endDate);
    void batchAuditComments(List<Long> commentIds, AuditStatus status);

    public boolean auditComment(Long commentId, Integer targetStatus);

    // 添加以下两个方法
    List<CommentDTO> getCommentsByPostId(Long postId);
    void addComment(Comments comment, Long userId);

    /**
     * 【重要】获取帖子的树形评论列表
     */
    List<CommentDTO> getTreeComments(Long postId);

    /**
     * 【重要】用户发表评论（含回复逻辑）
     */
    void postComment(Comments comment, Long userId);
}
