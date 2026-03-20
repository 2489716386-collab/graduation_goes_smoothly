package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.Comments;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.enums.ReportStatus;
import org.project.pet_health.enums.TargetType;
import org.project.pet_health.mapper.CommentsMapper;
import org.project.pet_health.mapper.ReportsMapper;
import org.project.pet_health.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentsService {

    @Autowired
    private ReportsMapper reportsMapper;

    @Override
    public Page<Comments> getAdminPage(Integer pageNum, Integer pageSize, Integer status, String content, String startDate, String endDate) {
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
    public void batchAuditComments(List<Long> commentIds, Integer status) {
        if (commentIds == null || commentIds.isEmpty()) return;

        this.update(new LambdaUpdateWrapper<Comments>()
                .in(Comments::getCommentId, commentIds)
                .set(Comments::getStatus, status));

        // 联动更新举报状态
        ReportStatus reportStatus = null;;
        if (status == 2) reportStatus = ReportStatus.DELETED;
        else if (status == 1) reportStatus = ReportStatus.IGNORED;

        if (reportStatus != null) {
            reportsMapper.update(null, new LambdaUpdateWrapper<Reports>()
                    .eq(Reports::getTargetType, TargetType.COMMENT) // 假设 TargetType: 2代表评论
                    .in(Reports::getTargetId, commentIds)
                    .set(Reports::getStatus, reportStatus));
        }
    }
}
