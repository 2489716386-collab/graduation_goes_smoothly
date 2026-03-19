package org.project.pet_health.service.impl;

import org.project.pet_health.entity.Reports;
import org.project.pet_health.mapper.ReportsMapper;
import org.project.pet_health.service.ReportsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户举报记录表 服务实现类
 * </p>
 *
 * @author weiling
 * @since 2026-03-19
 */
@Service
public class ReportsServiceImpl extends ServiceImpl<ReportsMapper, Reports> implements ReportsService {

}
