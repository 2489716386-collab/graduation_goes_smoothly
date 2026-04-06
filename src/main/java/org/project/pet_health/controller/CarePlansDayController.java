package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.project.pet_health.common.Result;
import org.project.pet_health.dto.CarePlansDayProgressDTO;
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

    /**
     * 查询宠物今日任务及百分比进度
     */
    @GetMapping("/today-plan/{petId}")
    public Result<CarePlansDayProgressDTO> getTodayPlan(@PathVariable Long petId) {
        CarePlansDayProgressDTO dto = carePlansDayService.getTodayPlanProgress(petId);
        return Result.success(dto);
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
    /**
     * 查询指定周的详细日计划（用于查看历史详情）
     */

//    @GetMapping("/week-details/{weekPlanId}")
//    public Result<List<CarePlansDayEntity>> getDayPlansByWeek(@PathVariable Long weekPlanId) {
//        List<CarePlansDayEntity> list = carePlansDayService.list(
//                new LambdaQueryWrapper<CarePlansDayEntity>()
//                        .eq(CarePlansDayEntity::getWeekPlanId, weekPlanId)
//                        .orderByAsc(CarePlansDayEntity::getPlanDate)
//        );
//        return Result.success(list);
//    }

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
