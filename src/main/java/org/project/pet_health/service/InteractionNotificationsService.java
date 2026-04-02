package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.InteractionNotificationsEntity;
import org.project.pet_health.dto.InteractionNoticeDTO;

import java.util.List;

public interface InteractionNotificationsService extends IService<InteractionNotificationsEntity> {

    /**
     * 发送互动通知（供点赞、评论等业务调用）
     * @param receiverId 接收者ID
     * @param senderId   发送者ID
     * @param type       类型: LIKE, COMMENT, FAVORITE
     * @param postId     关联帖子ID
     * @param content    附加内容（如评论详情）
     */
    void sendNotice(Long receiverId, Long senderId, String type, Long postId, String content);

    /**
     * 获取当前用户的通知列表
     */
    Page<InteractionNoticeDTO> getNoticeList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取未读总数
     */
    Long getUnreadCount(Long userId);

    /**
     * 一键已读
     */
    void markAllAsRead(Long userId);

    List<InteractionNoticeDTO> getMyNotices(Long userId);
}
