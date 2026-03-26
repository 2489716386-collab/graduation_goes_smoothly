package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.enums.AuditStatus;
import org.project.pet_health.enums.TargetType;

public interface ReportsService extends IService<Reports> {
    // 后台分页查询举报
    Page<Reports> getAdminPage(Integer pageNum, Integer pageSize, Integer targetType, Integer status, String reason, String startDate, String endDate);

    // 用户端：新增举报记录 (触发计数及状态拦截逻辑)
    void addUserReport(Reports report);

    /**
     * 根据审核结果，同步更新相关的举报记录状态
     * @param targetId 目标ID (动态ID或评论ID)
     * @param targetType 目标类型 (动态/评论)
     * @param auditStatus 审核结果状态
     */
    void syncReportStatusAfterAudit(Long targetId, TargetType targetType, AuditStatus auditStatus);}
