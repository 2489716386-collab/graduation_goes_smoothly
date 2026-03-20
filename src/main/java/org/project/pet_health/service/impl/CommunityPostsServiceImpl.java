package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.enums.ReportStatus;
import org.project.pet_health.enums.TargetType;
import org.project.pet_health.mapper.CommunityPostsMapper;
import org.project.pet_health.mapper.ReportsMapper;
import org.project.pet_health.service.CommunityPostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CommunityPostsServiceImpl extends ServiceImpl<CommunityPostsMapper, CommunityPosts> implements CommunityPostsService {

    @Autowired
    private ReportsMapper reportsMapper;

    @Override
    public Page<CommunityPosts> getAdminPage(Integer pageNum, Integer pageSize, Integer postType, Integer status, String content, String startDate, String endDate) {
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
    public void batchAuditPosts(List<Long> postIds, Integer status) {
        if (postIds == null || postIds.isEmpty()) return;

        // 1. 批量修改动态表状态 (例如：1=已发布, 2=违规拦截)
        this.update(new LambdaUpdateWrapper<CommunityPosts>()
                .in(CommunityPosts::getPostId, postIds)
                .set(CommunityPosts::getStatus, status));

        // 2. 【状态联动】同步修改举报记录表中的处理状态
        ReportStatus reportStatus = null;
        if (status == 2) {
            reportStatus = ReportStatus.DELETED;; // 如果动态被拦截，举报状态变为：2-已删除内容
        } else if (status == 1) {
            reportStatus = ReportStatus.IGNORED; // 如果动态恢复发布，举报状态变为：1-已忽略
        }

        if (reportStatus != null) {
            reportsMapper.update(null, new LambdaUpdateWrapper<Reports>()
                    .eq(Reports::getTargetType, TargetType.POST)
                    .in(Reports::getTargetId, postIds)
                    .set(Reports::getStatus, reportStatus));
        }
    }
}
