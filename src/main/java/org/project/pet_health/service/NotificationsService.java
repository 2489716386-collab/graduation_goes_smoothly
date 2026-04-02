package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.Notifications;
import org.project.pet_health.enums.NotificationType;

import java.util.List;

public interface NotificationsService extends IService<Notifications> {
    // 后台分页条件查询通知
    Page<Notifications> getAdminPage(Integer pageNum, Integer pageSize, String content, String startDate, String endDate, NotificationType type, Integer noticeId);

    List<Notifications> getUserNoticesByToken(String token);

}
