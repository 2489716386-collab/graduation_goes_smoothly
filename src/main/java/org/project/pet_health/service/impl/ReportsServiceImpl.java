package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.Comments;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.enums.ReportStatus;
import org.project.pet_health.enums.TargetType;
import org.project.pet_health.mapper.CommentsMapper;
import org.project.pet_health.mapper.CommunityPostsMapper;
import org.project.pet_health.mapper.ReportsMapper;
import org.project.pet_health.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ReportsServiceImpl extends ServiceImpl<ReportsMapper, Reports> implements ReportsService {

    @Autowired
    private CommunityPostsMapper postsMapper;
    @Autowired
    private CommentsMapper commentsMapper;

    @Override
    public Page<Reports> getAdminPage(Integer pageNum, Integer pageSize, Integer targetType, Integer status, String reason, String startDate, String endDate) {
        LambdaQueryWrapper<Reports> wrapper = new LambdaQueryWrapper<>();
        if (targetType != null) wrapper.eq(Reports::getTargetType, targetType);
        if (status != null) wrapper.eq(Reports::getStatus, status);
        if (StringUtils.hasText(reason)) wrapper.like(Reports::getReason, reason);
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            wrapper.between(Reports::getCreateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        }
        wrapper.orderByDesc(Reports::getReportId);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserReport(Reports report) {
        // 1. 使用枚举：保存用户的举报记录，默认状态为 0-待处理
        report.setStatus(ReportStatus.PENDING);
        this.save(report);

        // 2. 使用枚举判断类型，反向增加动态或评论的 report_count
        if (TargetType.POST.equals(report.getTargetType())) {
            CommunityPosts post = postsMapper.selectById(report.getTargetId());
            if (post != null) {
                post.setReportCount(post.getReportCount() + 1);
                if (post.getReportCount() >= 5) {
                    post.setStatus(0); // 帖子变为待审核
                }
                postsMapper.updateById(post);
            }
        } else if (TargetType.COMMENT.equals(report.getTargetType())) {
            Comments comment = commentsMapper.selectById(report.getTargetId());
            if (comment != null) {
                comment.setReportCount(comment.getReportCount() + 1);
                if (comment.getReportCount() >= 5) {
                    comment.setStatus(0); // 评论变为待审核
                }
                commentsMapper.updateById(comment);
            }
        }
    }
}
