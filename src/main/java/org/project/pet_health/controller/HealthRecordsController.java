package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.HealthRecords;
import org.project.pet_health.service.HealthRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health-records")
@Tag(name = "小程序-宠物健康记录")
public class HealthRecordsController {

    @Autowired
    private HealthRecordsService healthRecordsService;

    @GetMapping("/user/list")
    @Operation(summary = "获取某只宠物的体检记录")
    public Result list(@RequestParam Long petId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(healthRecordsService.getPetHealthRecords(petId, userId));
    }

    @PostMapping("/user/add")
    @Operation(summary = "上传体检报告")
    public Result add(@RequestBody HealthRecords record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        healthRecordsService.addHealthRecord(record, userId);
        return Result.success("健康记录上传成功");
    }
}
