package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.enums.AuditStatus;
import org.project.pet_health.enums.ReportStatus;
import org.project.pet_health.enums.TargetType;
import org.project.pet_health.mapper.CommunityPostsMapper;
import org.project.pet_health.mapper.ReportsMapper;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CommunityPostsServiceImpl extends ServiceImpl<CommunityPostsMapper, CommunityPosts> implements CommunityPostsService {

    @Autowired
    private ReportsMapper reportsMapper;

    @Autowired
    private ReportsService reportsService;

    @Override
    public Page<CommunityPosts> getAdminPage(Integer pageNum, Integer pageSize, Integer postType, AuditStatus status, String content, String startDate, String endDate) {
        LambdaQueryWrapper<CommunityPosts> wrapper = new LambdaQueryWrapper<>();

        // 【核心】过滤掉 media_urls 字段不查询，减少宽带消耗
        wrapper.select(CommunityPosts.class, info -> !info.getColumn().equals("media_urls"));

        if (postType != null) wrapper.eq(CommunityPosts::getPostType, postType);
        if (status != null) wrapper.eq(CommunityPosts::getStatus, status);
        if (StringUtils.hasText(content)) wrapper.like(CommunityPosts::getContent, content);
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            wrapper.between(CommunityPosts::getCreateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        }
        wrapper.orderByDesc(CommunityPosts::getPostId);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAuditPosts(List<Long> postIds, AuditStatus status) {
        if (postIds == null || postIds.isEmpty()) return;

        // 1. 批量修改动态表状态 (例如：1=已发布, 2=违规拦截)
        this.update(new LambdaUpdateWrapper<CommunityPosts>()
                .in(CommunityPosts::getPostId, postIds)
                .set(CommunityPosts::getStatus, status));

        // 2. 【状态联动】同步修改举报记录表中的处理状态
        ReportStatus reportStatus = null;
        if (AuditStatus.REJECTED.equals(status)) {
            reportStatus = ReportStatus.DELETED;; // 如果动态被拦截，举报状态变为：2-已删除内容
        } else if (AuditStatus.APPROVED.equals(status)) {
            reportStatus = ReportStatus.IGNORED; // 如果动态恢复发布，举报状态变为：1-已忽略
        }

        if (reportStatus != null) {
            reportsMapper.update(null, new LambdaUpdateWrapper<Reports>()
                    .eq(Reports::getTargetType, TargetType.POST)
                    .in(Reports::getTargetId, postIds)
                    .set(Reports::getStatus, reportStatus));
        }
    }

    @Override
    public void addUserPost(CommunityPosts post, Long userId) {
        post.setUserId(userId);
        // 使用我们之前定义好的枚举：默认状态为已发布
        // TODO: 如果你接了敏感词过滤接口，可以在这里判断，有敏感词就置为 PENDING
        post.setStatus(AuditStatus.APPROVED);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setReportCount(0);
        this.save(post);
    }

    @Override
    public void deleteUserPost(Long postId, Long userId) {
        // 只能删除自己的帖子
        this.remove(new LambdaQueryWrapper<CommunityPosts>()
                .eq(CommunityPosts::getPostId, postId)
                .eq(CommunityPosts::getUserId, userId));
    }

    @Override
    public Page<CommunityPosts> getCommunityFeed(Integer pageNum, Integer pageSize) {
        // 社区主页瀑布流：只查状态为 APPROVED (已发布) 的帖子，按时间倒序
        return this.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<CommunityPosts>()
                        .eq(CommunityPosts::getStatus, AuditStatus.APPROVED)
                        .orderByDesc(CommunityPosts::getCreateTime));
    }

    /**
     * 审核动态 (支持单条或批量调用)
     * @param postId 动态ID
     * @param targetStatus 目标状态 (1: 已发布, 2: 违规拦截)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditPost(Long postId, Integer targetStatus) {
        CommunityPosts post = this.getById(postId);
        if (post == null) return false;

        // 【核心修复】：将前端传来的 Integer 数字，翻译成我们的枚举对象
        AuditStatus newStatus = null;
        for (AuditStatus as : AuditStatus.values()) {
            if (as.getValue().equals(targetStatus)) { // 这里用 getValue() 匹配前端数字
                newStatus = as;
                break;
            }
        }
        if (newStatus == null) return false; // 如果乱传数字，直接拒绝

        // 【解决报错1】：类型匹配了！把翻译好的枚举对象放进去
        post.setStatus(newStatus);
        boolean updatePost = this.updateById(post);

        // 冗余代码被缩减为这一行优雅的调用！
        if (updatePost && post.getReportCount() != null && post.getReportCount() >= 5) {
            reportsService.syncReportStatusAfterAudit(postId, TargetType.POST, newStatus);
        }
        return updatePost;
    }
}
