package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.Reports;

public interface ReportsService extends IService<Reports> {
    // 后台分页查询举报
    Page<Reports> getAdminPage(Integer pageNum, Integer pageSize, Integer targetType, Integer status, String reason, String startDate, String endDate);

    // 用户端：新增举报记录 (触发计数及状态拦截逻辑)
    void addUserReport(Reports report);
}
