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

    @PostMapping("/preview")
    public Result<String> getAiPreview(@RequestBody PlanGenerateDTO dto) {
        return Result.success(weekService.generatePreview(dto));
    }

    @PostMapping("/import")
    public Result<Void> doImport(@RequestBody PlanImportDTO dto) {
        weekService.confirmAndImport(dto);
        return Result.success();
    }


    //查询历史周计划列表（用于 history.vue 列表展示）
    @GetMapping("/history/{petId}")
    public Result<List<CarePlansWeekEntity>> getHistoryWeeks(@PathVariable Long petId) {
        List<CarePlansWeekEntity> list = weekService.list(
                new LambdaQueryWrapper<CarePlansWeekEntity>()
                        .eq(CarePlansWeekEntity::getPetId, petId)
                        .eq(CarePlansWeekEntity::getIsCurrent, 0) // 仅查询历史记录
                        .orderByDesc(CarePlansWeekEntity::getCreateTime)
        );
        return Result.success(list);
    }
}
