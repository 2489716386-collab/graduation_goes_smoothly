package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.dto.CarePlansDayProgressDTO;
import org.project.pet_health.entity.CarePlansDayEntity;

/**
 * <p>
 * 每日养护打卡任务表 服务类
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
public interface CarePlansDayService extends IService<CarePlansDayEntity> {

    // 执行/取消打卡，并返回最新的进度百分比
    int toggleCheckIn(Long taskId);

    // 👇 添加这个方法：获取今日任务及进度
    CarePlansDayProgressDTO getTodayPlanProgress(Long petId);
}
