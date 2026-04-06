package org.project.pet_health.controller;

import org.project.pet_health.common.Result;
import org.project.pet_health.dto.PlanGenerateDTO;
import org.project.pet_health.dto.PlanImportDTO;
import org.project.pet_health.service.CarePlansWeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
