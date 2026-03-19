package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.AdminLogs;
import org.project.pet_health.mapper.AdminLogsMapper;
import org.project.pet_health.service.AdminLogsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminLogsServiceImpl extends ServiceImpl<AdminLogsMapper, AdminLogs> implements AdminLogsService {

    @Override
    public Page<AdminLogs> getLogsPage(Integer pageNum, Integer pageSize, Long adminId, String action, String startDate, String endDate) {
        LambdaQueryWrapper<AdminLogs> wrapper = new LambdaQueryWrapper<>();

        // 精确匹配管理员ID
        if (adminId != null) {
            wrapper.eq(AdminLogs::getAdminId, adminId);
        }

        // 模糊查询操作内容
        if (StringUtils.hasText(action)) {
            wrapper.like(AdminLogs::getAction, action);
        }

        // 按照日期范围筛选 (假设前端传来的格式是 2024-05-01)
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            wrapper.between(AdminLogs::getCreateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        }

        // 按操作时间倒序排列，最新的在前面
        wrapper.orderByDesc(AdminLogs::getCreateTime);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }
}
