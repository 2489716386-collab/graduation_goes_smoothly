package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.AdminLogs;

public interface AdminLogsService extends IService<AdminLogs> {
    // 分页条件查询日志
    Page<AdminLogs> getLogsPage(Integer pageNum, Integer pageSize, Long adminId, String action, String startDate, String endDate);
}
