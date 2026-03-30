package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.Comments;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.enums.AuditStatus;
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
    public IPage<Reports> pageAdmin(Integer pageNum, Integer pageSize, String targetType, Integer status, String startDate, String endDate, String reason, Long targetId) {
        LambdaQueryWrapper<Reports> wrapper = new LambdaQueryWrapper<>();

        if (targetId != null) {
            wrapper.eq(Reports::getTargetId, targetId);
        }

        // 【关键修复 1】：将前端传来的 String 转换为 TargetType 枚举对象

        if (StringUtils.hasText(targetType)) {
            if ("post".equals(targetType)) {
                wrapper.eq(Reports::getTargetType, TargetType.POST);
            } else if ("comment".equals(targetType)) {
                wrapper.eq(Reports::getTargetType, TargetType.COMMENT);
            }
        }

        // 将前端传来的 Integer 转换为 ReportStatus 枚举对象
        if (status != null) {
            if (status == 0) {
                wrapper.eq(Reports::getStatus, ReportStatus.PENDING);
            } else if (status == 1) {
                wrapper.eq(Reports::getStatus, ReportStatus.IGNORED);
            } else if (status == 2) {
                wrapper.eq(Reports::getStatus, ReportStatus.DELETED);
            }
        }

        // 4. 举报理由模糊查询
        if (StringUtils.hasText(reason)) {
            wrapper.like(Reports::getReason, reason);
        }

        // 5. 日期范围查询
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
                    post.setStatus(AuditStatus.PENDING); // 帖子变为待审核
                }
                postsMapper.updateById(post);
            }
        } else if (TargetType.COMMENT.equals(report.getTargetType())) {
            Comments comment = commentsMapper.selectById(report.getTargetId());
            if (comment != null) {
                comment.setReportCount(comment.getReportCount() + 1);
                if (comment.getReportCount() >= 5) {
                    comment.setStatus(AuditStatus.PENDING); // 评论变为待审核
                }
                commentsMapper.updateById(comment);
            }
        }
    }

    @Override
    public void syncReportStatusAfterAudit(Long targetId, TargetType targetType, AuditStatus auditStatus) {
        // 1. 根据审核结果判定举报记录的目标状态
        ReportStatus targetReportStatus = (auditStatus == AuditStatus.REJECTED) ?
                ReportStatus.DELETED : ReportStatus.IGNORED;

        // 2. 批量更新
        LambdaUpdateWrapper<Reports> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Reports::getTargetId, targetId)
                .eq(Reports::getTargetType, targetType)
                .eq(Reports::getStatus, ReportStatus.PENDING);

        Reports updateReport = new Reports();
        updateReport.setStatus(targetReportStatus);

        // 因为这里本身就是 ReportsServiceImpl，直接调用 this.update 即可
        this.update(updateReport, wrapper);
    }
}
