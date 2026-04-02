package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.dto.InteractionNoticeDTO;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.InteractionNotificationsEntity;
import org.project.pet_health.entity.Users;
import org.project.pet_health.mapper.InteractionNotificationsMapper;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.service.InteractionNotificationsService;
import org.project.pet_health.service.UsersService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InteractionNotificationsServiceImpl extends ServiceImpl<InteractionNotificationsMapper, InteractionNotificationsEntity> implements InteractionNotificationsService {

    @Autowired
    private UsersService usersService;

    @Lazy // 延迟加载，防止与PostsService产生循环依赖
    @Autowired
    private CommunityPostsService postsService;

    @Override
    public void sendNotice(Long receiverId, Long senderId, String type, Long postId, String content) {
        // 🚨 逻辑防御：如果是自己点赞/评论自己，不发通知
        if (receiverId.equals(senderId)) return;

        InteractionNotificationsEntity notice = new InteractionNotificationsEntity();
        notice.setReceiverId(receiverId);
        notice.setSenderId(senderId);
        notice.setType(type);
        notice.setPostId(postId);
        notice.setContent(content); // 评论内容或null
        notice.setIsRead(false);
        this.save(notice);
    }

    @Override
    public Page<InteractionNoticeDTO> getNoticeList(Long userId, Integer pageNum, Integer pageSize) {
        Page<InteractionNotificationsEntity> page = new Page<>(pageNum, pageSize);
        this.page(page, new LambdaQueryWrapper<InteractionNotificationsEntity>()
                .eq(InteractionNotificationsEntity::getReceiverId, userId)
                .orderByDesc(InteractionNotificationsEntity::getCreateTime));

        if (page.getRecords().isEmpty()) return new Page<>();

        // 1. 批量提取相关ID
        List<Long> senderIds = page.getRecords().stream().map(InteractionNotificationsEntity::getSenderId).distinct().collect(Collectors.toList());
        List<Long> postIds = page.getRecords().stream().map(InteractionNotificationsEntity::getPostId).distinct().collect(Collectors.toList());

        // 2. 批量查询用户信息和帖子信息
        Map<Long, Users> userMap = usersService.listByIds(senderIds).stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));
        Map<Long, CommunityPosts> postMap = postsService.listByIds(postIds).stream()
                .collect(Collectors.toMap(CommunityPosts::getPostId, p -> p));

        // 3. 组装数据
        List<InteractionNoticeDTO> dtoList = page.getRecords().stream().map(record -> {
            InteractionNoticeDTO dto = new InteractionNoticeDTO();
            dto.setId(record.getId());
            dto.setType(record.getType());
            dto.setPostId(record.getPostId());

            // ⚠️ 关键修复：将 Boolean 转换为 Integer (false -> 0, true -> 1)
            dto.setIsRead(record.getIsRead() != null && record.getIsRead() ? 1 : 0);

            dto.setCreateTime(record.getCreateTime());

            // 填充发送者（操作人）资料
            Users sender = userMap.get(record.getSenderId());
            if (sender != null) {
                dto.setSenderId(sender.getUserId());
                dto.setSenderNickname(sender.getNickname());
                dto.setSenderAvatar(sender.getAvatarUrl());
            }

            // 获取被操作的帖子摘要
            CommunityPosts post = postMap.get(record.getPostId());
            String summary = "已删除的帖子";
            if (post != null) {
                String c = post.getContent();
                summary = (c != null && c.length() > 15) ? c.substring(0, 15) + "..." : c;
            }
            dto.setPostSummary(summary);

            // 根据类型组装文案
            switch (record.getType()) {
                case "LIKE": dto.setActionText("赞了你的动态"); break;
                case "FAVORITE": dto.setActionText("收藏了你的动态"); break;
                case "COMMENT": dto.setActionText("评论了你: " + record.getContent()); break;
                default: dto.setActionText("与你进行了互动");
            }
            return dto;
        }).collect(Collectors.toList());

        Page<InteractionNoticeDTO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(dtoList);
        return result;
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return this.count(new LambdaQueryWrapper<InteractionNotificationsEntity>()
                .eq(InteractionNotificationsEntity::getReceiverId, userId)
                // ⚠️ 实体类是 Boolean，用 false 查询未读
                .eq(InteractionNotificationsEntity::getIsRead, false));
    }

    @Override
    public void markAllAsRead(Long userId) {
        this.update(new LambdaUpdateWrapper<InteractionNotificationsEntity>()
                .eq(InteractionNotificationsEntity::getReceiverId, userId)
                // ⚠️ 实体类是 Boolean，用 false 和 true 更新状态
                .eq(InteractionNotificationsEntity::getIsRead, false)
                .set(InteractionNotificationsEntity::getIsRead, true));
    }

    @Override
    public List<InteractionNoticeDTO> getMyNotices(Long userId) {
        // 1. 获取最近50条通知
        List<InteractionNotificationsEntity> list = this.list(new LambdaQueryWrapper<InteractionNotificationsEntity>()
                .eq(InteractionNotificationsEntity::getReceiverId, userId)
                .orderByDesc(InteractionNotificationsEntity::getCreateTime)
                .last("limit 50"));

        if (list.isEmpty()) return List.of();

        // 2. 批量获取发送者信息和帖子信息
        Set<Long> senderIds = list.stream().map(InteractionNotificationsEntity::getSenderId).collect(Collectors.toSet());
        Set<Long> postIds = list.stream().map(InteractionNotificationsEntity::getPostId).collect(Collectors.toSet());

        Map<Long, Users> userMap = usersService.listByIds(senderIds).stream().collect(Collectors.toMap(Users::getUserId, u -> u));
        Map<Long, CommunityPosts> postMap = postsService.listByIds(postIds).stream().collect(Collectors.toMap(CommunityPosts::getPostId, p -> p));

        // 3. 组装 DTO
        List<InteractionNoticeDTO> dtos = list.stream().map(n -> {
            InteractionNoticeDTO dto = new InteractionNoticeDTO();
            BeanUtils.copyProperties(n, dto);

            Users sender = userMap.get(n.getSenderId());
            if (sender != null) {
                dto.setSenderNickname(sender.getNickname());
                dto.setSenderAvatar(sender.getAvatarUrl());
            }

            CommunityPosts post = postMap.get(n.getPostId());
            if (post != null) {
                String raw = post.getContent();
                dto.setPostContentTeaser(raw.length() > 20 ? raw.substring(0, 20) + "..." : raw);
            }
            return dto;
        }).collect(Collectors.toList());

        // 4. 【关键】标记为已读（点击进入列表即视为全部阅读）
        this.update().set("is_read", true)
                .eq("receiver_id", userId)
                .eq("is_read", false)
                .update();

        return dtos;
    }
}
