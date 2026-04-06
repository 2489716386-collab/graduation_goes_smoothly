package org.project.pet_health.controller;

import org.project.pet_health.common.Result;
import org.project.pet_health.dto.CarePlansDayProgressDTO;
import org.project.pet_health.service.CarePlansDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 查询宠物今日任务及百分比进度
     */
    @GetMapping("/today-plan/{petId}")
    public Result<CarePlansDayProgressDTO> getTodayPlan(@PathVariable Long petId) {
        return Result.success(carePlansDayService.getTodayPlan(petId));
    }

    /**
     * 点击任务打卡
     * 返回值：该宠物今日最新的百分比进度
     */
    @PostMapping("/check-in/{taskId}")
    public Result<Integer> doCheckIn(@PathVariable Long taskId) {
        int newProgress = carePlansDayService.toggleCheckIn(taskId);
        return Result.success(newProgress);
    }
}
