package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.dto.CommentDTO;
import org.project.pet_health.entity.Comments;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.entity.Users;
import org.project.pet_health.enums.AuditStatus;
import org.project.pet_health.enums.ReportStatus;
import org.project.pet_health.enums.TargetType;
import org.project.pet_health.mapper.CommentsMapper;
import org.project.pet_health.mapper.ReportsMapper;
import org.project.pet_health.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentsService {

    @Autowired
    private ReportsMapper reportsMapper;
    @Autowired
    private UsersService usersService;
    @Autowired
    private InteractionNotificationsService noticeService;
    @Autowired
    private CommunityPostsService postsService;

    @Override
    public Page<Comments> getAdminPage(Integer pageNum, Integer pageSize,AuditStatus status, String content, String startDate, String endDate) {
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Comments::getStatus, status);
        if (StringUtils.hasText(content)) wrapper.like(Comments::getContent, content);
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            wrapper.between(Comments::getCreateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        }
        wrapper.orderByDesc(Comments::getCommentId);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAuditComments(List<Long> commentIds, AuditStatus status) {
        if (commentIds == null || commentIds.isEmpty()) return;

        this.update(new LambdaUpdateWrapper<Comments>()
                .in(Comments::getCommentId, commentIds)
                .set(Comments::getStatus, status));

        // 联动更新举报状态
        ReportStatus reportStatus = null;;
        if (AuditStatus.REJECTED.equals(status)) reportStatus = ReportStatus.DELETED;
        else if (AuditStatus.APPROVED.equals(status)) reportStatus = ReportStatus.IGNORED;

        if (reportStatus != null) {
            reportsMapper.update(null, new LambdaUpdateWrapper<Reports>()
                    .eq(Reports::getTargetType, TargetType.COMMENT) // 假设 TargetType: 2代表评论
                    .in(Reports::getTargetId, commentIds)
                    .set(Reports::getStatus, reportStatus));
        }
    }

    @Autowired
    private ReportsService reportsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditComment(Long commentId, Integer targetStatus) {
        Comments comment = this.getById(commentId);
        if (comment == null) return false;

        // 将 Integer 翻译为枚举
        AuditStatus newStatus = null;
        for (AuditStatus as : AuditStatus.values()) {
            if (as.getValue().equals(targetStatus)) {
                newStatus = as;
                break;
            }
        }
        if (newStatus == null) return false;

        comment.setStatus(newStatus);
        boolean updateComment = this.updateById(comment);

        // 同样缩减为一行调用！
        if (updateComment && comment.getReportCount() != null && comment.getReportCount() >= 5) {
            reportsService.syncReportStatusAfterAudit(commentId, TargetType.COMMENT, newStatus);
        }
        return updateComment;
    }

    @Override
    public List<CommentDTO> getTreeComments(Long postId) {
        // 1. 查出该帖子下所有审核通过的评论
        List<Comments> allComments = this.list(new LambdaQueryWrapper<Comments>()
                .eq(Comments::getPostId, postId)
                .eq(Comments::getStatus, AuditStatus.APPROVED)
                .orderByAsc(Comments::getCreateTime));

        if (allComments.isEmpty()) return new ArrayList<>();

        // 2. 批量获取涉及到的用户 ID（包括评论人和被回复人）
        Set<Long> uIds = new HashSet<>();
        allComments.forEach(c -> {
            uIds.add(c.getUserId());
            if (c.getParentId() != null && c.getParentId() != 0) {
                // 如果你的表里存了 reply_to_user_id，也要加进来
                // 暂时通过父评论找被回复人
            }
        });
        Map<Long, Users> userMap = usersService.listByIds(uIds).stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));

        // 3. 将 Entity 转换为 DTO 并补全用户信息
        List<CommentDTO> allDTOs = allComments.stream().map(c -> {
            CommentDTO dto = new CommentDTO();
            BeanUtils.copyProperties(c, dto);
            Users user = userMap.get(c.getUserId());
            if (user != null) {
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());

        // 4. 【核心】递归或两层嵌套构建树
        // 这里采用主流的两层展示结构（所有回复都属于一级评论）
        List<CommentDTO> rootComments = allDTOs.stream()
                .filter(d -> d.getParentId() == null || d.getParentId() == 0)
                .collect(Collectors.toList());

        List<CommentDTO> subComments = allDTOs.stream()
                .filter(d -> d.getParentId() != null && d.getParentId() != 0)
                .collect(Collectors.toList());

        for (CommentDTO root : rootComments) {
            List<CommentDTO> replies = subComments.stream()
                    .filter(sub -> sub.getParentId().equals(root.getCommentId()))
                    .collect(Collectors.toList());
            root.setReplies(replies);
        }

        return rootComments;
    }

    @Override
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        // 1. 查询该动态下所有审核通过的评论
        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getPostId, postId)
                .eq(Comments::getStatus, AuditStatus.APPROVED) // 只看已发布的
                .orderByDesc(Comments::getCreateTime);
        List<Comments> list = this.list(wrapper);

        if (list.isEmpty()) return new ArrayList<>();

        // 2. 批量获取用户信息，避免循环查库
        List<Long> userIds = list.stream().map(Comments::getUserId).distinct().collect(Collectors.toList());
        Map<Long, Users> userMap = usersService.listByIds(userIds).stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));

        // 3. 组装 DTO
        return list.stream().map(c -> {
            CommentDTO dto = new CommentDTO();
            BeanUtils.copyProperties(c, dto);
            Users user = userMap.get(c.getUserId());
            if (user != null) {
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addComment(Comments comment, Long userId) {
        comment.setUserId(userId);
        comment.setStatus(AuditStatus.APPROVED); // 也可以设为待审核，这里演示直接发布
        comment.setLikeCount(0);
        this.save(comment);

        // 1. 同步更新帖子的评论计数
        postsService.update().setSql("comments_count = comments_count + 1")
                .eq("post_id", comment.getPostId()).update();

        // 2. 触发互动通知
        CommunityPosts post = postsService.getById(comment.getPostId());
        if (post != null) {
            noticeService.sendNotice(post.getUserId(), userId, "COMMENT", comment.getPostId(), comment.getContent());
        }
    }

    @Override
    @Transactional
    public void postComment(Comments comment, Long userId) {
        comment.setUserId(userId);
        comment.setCreateTime(null); // 交给数据库填充或自动填
        comment.setStatus(AuditStatus.APPROVED); // 默认通过，也可以设为待审核
        this.save(comment);

        // 1. 更新帖子评论数
        postsService.update().setSql("comments_count = comments_count + 1")
                .eq("post_id", comment.getPostId()).update();

        // 2. 发送通知
        // 如果是评论帖子，给帖子作者发
        // 如果是回复评论，给原评论人发
        Long receiverId;
        if (comment.getParentId() == null || comment.getParentId() == 0) {
            receiverId = postsService.getById(comment.getPostId()).getUserId();
        } else {
            receiverId = this.getById(comment.getParentId()).getUserId();
        }

        noticeService.sendNotice(receiverId, userId, "COMMENT", comment.getPostId(), comment.getContent());
    }
}
