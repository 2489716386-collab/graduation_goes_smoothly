package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.CarePlansDayEntity;
import org.project.pet_health.service.CarePlansDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 每日养护打卡任务表 前端控制器
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
@RestController
@RequestMapping("/care-plans-day")
public class CarePlansDayController {

    @Autowired
    private CarePlansDayService carePlansDayService;

    // 👇 1. 获取今日任务列表
    @GetMapping("/today-plan/{petId}")
    public Result<List<CarePlansDayEntity>> getTodayPlan(@PathVariable Long petId) {
        List<CarePlansDayEntity> tasks = carePlansDayService.getTodayTasks(petId);
        return Result.success(tasks);
    }

    // 👇 2. 新增：单条任务打卡接口
    @PostMapping("/check-in/{taskId}")
    public Result<Boolean> checkInTask(@PathVariable Long taskId) {
        boolean success = carePlansDayService.checkInTask(taskId);
        return Result.success(success);
    }
    /**
     * 查询指定周的详细日计划（用于查看历史详情）
     */
        // 👇 查询这周每天的具体任务
    @GetMapping("/week-details/{weekPlanId}")
    public Result<List<CarePlansDayEntity>> getWeekDetails(@PathVariable Long weekPlanId) {
        List<CarePlansDayEntity> list = carePlansDayService.list(
                new LambdaQueryWrapper<CarePlansDayEntity>()
                        .eq(CarePlansDayEntity::getWeekPlanId, weekPlanId)
                        .orderByAsc(CarePlansDayEntity::getPlanDate) // 必须按日期排序，前端才能正确分组
        );
        return Result.success(list);
    }
}
