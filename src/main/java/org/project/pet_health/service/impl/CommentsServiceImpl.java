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
import org.project.pet_health.utils.SensitiveWordFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

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

        // 2. 批量获取涉及到的用户 ID
        Set<Long> uIds = allComments.stream().map(Comments::getUserId).collect(Collectors.toSet());
        Map<Long, Users> userMap = usersService.listByIds(uIds).stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));

        // 3. 将 Entity 转换为 DTO 并补全基本用户信息
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

        // 4. 建立 commentId -> DTO 的映射，方便快速查找被回复人
        Map<Long, CommentDTO> dtoMap = allDTOs.stream()
                .collect(Collectors.toMap(CommentDTO::getCommentId, d -> d));

        // 5. 分离一级评论和二级评论，并补全二级评论的 replyTo 信息
        List<CommentDTO> rootComments = new ArrayList<>();
        List<CommentDTO> subComments = new ArrayList<>();

        for (CommentDTO dto : allDTOs) {
            if (dto.getParentId() == null || dto.getParentId() == 0) {
                rootComments.add(dto);
            } else {
                // 【核心修复】通过 parentId 找到父评论，提取被回复人的昵称和ID
                CommentDTO parentDto = dtoMap.get(dto.getParentId());
                if (parentDto != null) {
                    dto.setReplyToUserId(parentDto.getUserId());
                    dto.setReplyToNickname(parentDto.getNickname());
                }
                subComments.add(dto);
            }
        }

        // 6. 将二级评论塞入对应的一级评论的 replies 列表中
        // 6. 🚀 完美修复楼中楼：将所有二级/三级评论，全部塞入它们【最顶级】的一级评论的 replies 列表中
        for (CommentDTO sub : subComments) {
            // 向上追溯，找到它的顶级根评论 ID
            Long currentParentId = sub.getParentId();

            while (currentParentId != null && currentParentId != 0) {
                CommentDTO parent = dtoMap.get(currentParentId);
                if (parent == null) break;

                // 如果父评论的 parentId 为空或 0，说明这个父评论就是顶级评论！
                if (parent.getParentId() == null || parent.getParentId() == 0) {
                    Long rootId = parent.getCommentId();

                    // 找到根评论对象，把当前子评论塞进去
                    for (CommentDTO root : rootComments) {
                        if (root.getCommentId().equals(rootId)) {
                            if (root.getReplies() == null) {
                                root.setReplies(new ArrayList<>());
                            }
                            root.getReplies().add(sub);
                            break;
                        }
                    }
                    break;
                }
                // 还没到顶，继续往上找
                currentParentId = parent.getParentId();
            }
        }

        return rootComments;
    } // getTreeComments 方法结束

    @Override
    @Transactional
    public void postComment(Comments comment, Long userId) {
        // 1. 补全评论的基础信息
        comment.setUserId(userId);
        comment.setCreateTime(null); // 让数据库自动生成时间
        // 1. 敏感词替换为 *
        if (StringUtils.hasText(comment.getContent())) {
            String filteredContent = sensitiveWordFilter.replaceSensitiveWord(comment.getContent());
            comment.setContent(filteredContent);
        }

        comment.setStatus(AuditStatus.APPROVED); // 默认审核通过
        // 点赞数必须初始化为0，否则插入数据库会报错
        comment.setLikeCount(0);

        // 执行保存
        this.save(comment);

        // 2. 💡 修复2：更安全的更新帖子评论数
        // 这里使用 update().setSql 是一种快捷方式。
        // 请确保你数据库 community_posts 表中，主键字段确实叫 post_id
        CommunityPosts post = postsService.getById(comment.getPostId());
        if (post != null) {
            int currentCount = post.getCommentCount() == null ? 0 : post.getCommentCount();
            post.setCommentCount(currentCount + 1);
            postsService.updateById(post);
        }

        // 3. 💡 修复3：极其严谨的通知发送逻辑（防止空指针异常）
        Long receiverId = null;

        if (comment.getParentId() == null || comment.getParentId() == 0) {
            // 如果是一级评论，通知发给【帖子作者】
            if (post != null) {
                receiverId = post.getUserId();
            }
        } else {
            // 如果是二级评论（回复），通知发给【原评论作者】
            Comments parentComment = this.getById(comment.getParentId());
            if (parentComment != null) {
                receiverId = parentComment.getUserId();
            }
        }

        // 只有明确知道接收人是谁，且不是自己回复自己时，才发通知
        if (receiverId != null && !receiverId.equals(userId)) {
            noticeService.sendNotice(receiverId, userId, "COMMENT", comment.getPostId(), comment.getContent());
        }
    }
}
