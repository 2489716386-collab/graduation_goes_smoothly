package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.Notifications;

public interface NotificationsService extends IService<Notifications> {
    // 后台分页条件查询通知
    Page<Notifications> getAdminPage(Integer pageNum, Integer pageSize, String content, String startDate, String endDate, Integer type);
}
