package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.Notifications;
import org.project.pet_health.enums.NotificationType;
import org.project.pet_health.mapper.NotificationsMapper;
import org.project.pet_health.service.NotificationsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationsServiceImpl extends ServiceImpl<NotificationsMapper, Notifications> implements NotificationsService {

    @Override
    public Page<Notifications> getAdminPage(Integer pageNum, Integer pageSize, String content, String startDate, String endDate, NotificationType type, Integer noticeId) {
        LambdaQueryWrapper<Notifications> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询通知正文
        if (StringUtils.hasText(content)) {
            wrapper.like(Notifications::getContent, content);
        }
        // 按通知类型筛选 (如: 1系统维护, 2违规提醒)
        if (type != null) {
            wrapper.eq(Notifications::getNoticeType, type);
        }
        // 按发送时间段筛选
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            wrapper.between(Notifications::getCreateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        }

        // 按通知ID倒序排列
        wrapper.orderByDesc(Notifications::getNoticeId);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }
}
