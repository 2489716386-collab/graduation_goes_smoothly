package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.CarePlansDayEntity;

import java.util.List;

/**
 * <p>
 * 每日养护打卡任务表 服务类
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
public interface CarePlansDayService extends IService<CarePlansDayEntity> {

    // 1. 获取今日所有任务（取消百分比，直接返回列表）
    List<CarePlansDayEntity> getTodayTasks(Long petId);

    // 2. 新增：用户点击打卡单条任务
    boolean checkInTask(Long taskId);
}
