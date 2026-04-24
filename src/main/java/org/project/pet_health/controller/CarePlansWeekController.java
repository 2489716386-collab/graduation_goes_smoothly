package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.project.pet_health.common.Result;
import org.project.pet_health.dto.PlanGenerateDTO;
import org.project.pet_health.dto.PlanImportDTO;
import org.project.pet_health.entity.CarePlansWeekEntity;
import org.project.pet_health.service.CarePlansWeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * AI养护周计划主表 前端控制器
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
@RestController
@RequestMapping("/care-plans-week")
public class CarePlansWeekController {

    @Autowired
    private CarePlansWeekService weekService;


    // 👇 1. 解决 404 的关键：添加 AI 生成预览的 POST 接口
    @PostMapping("/generate-preview")
    public Result<String> generatePreview(@RequestBody PlanGenerateDTO dto) {
        // 调用 Service 层的逻辑
        String aiJsonResult = weekService.generatePreview(dto);
        return Result.success(aiJsonResult);
    }

    @PostMapping("/confirm-import")
    public Result<Boolean> confirmImport(@RequestBody PlanImportDTO dto) {

        // 1. 先独立调用 Service 的方法（因为它没有返回值，所以单独放一行执行）
        weekService.confirmAndImport(dto);

        // 2. 执行成功后，手动给前端返回一个 true (或者直接用无参的 Result.success())
        return Result.success(true);
    }

    @GetMapping("/current/{petId}")
    public Result<CarePlansWeekEntity> getCurrentPlan(@PathVariable Long petId) {
        // 调用我们刚刚写好的严格限制本周的方法 (注意你的 service 注入变量名如果是 weekService 就用 weekService)
        CarePlansWeekEntity plan = weekService.getActivePlan(petId);

        // 直接返回，如果本周没生成，plan 为 null，前端拿到 null 后就会显示"请更新计划"
        return Result.success(plan);
    }


    //查询历史周计划列表（用于 history.vue 列表展示）
    @GetMapping("/history-list/{petId}")
    public Result<List<CarePlansWeekEntity>> getHistoryList(@PathVariable Long petId) {
        List<CarePlansWeekEntity> history = weekService.list(
                new LambdaQueryWrapper<CarePlansWeekEntity>()
                        .eq(CarePlansWeekEntity::getPetId, petId)
                        .eq(CarePlansWeekEntity::getIsCurrent, 0) // 0代表历史计划
                        .orderByDesc(CarePlansWeekEntity::getCreateTime) // 按生成时间倒序排列
        );
        return Result.success(history);
    }
}
