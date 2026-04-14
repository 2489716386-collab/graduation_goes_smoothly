package org.project.pet_health.controller;

import jakarta.annotation.Resource;
import org.project.pet_health.common.AdminAPI;
import org.project.pet_health.common.Result;
import org.project.pet_health.dto.DashboardStatsDTO;
import org.project.pet_health.dto.TrendDTO;
import org.project.pet_health.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@AdminAPI
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/trend")
    public Result<TrendDTO> getTrendData() {
        // 调用 Service 层处理具体的业务逻辑
        TrendDTO trendData = dashboardService.getTrendData();
        return Result.success(trendData);
    }

    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getStatsData() { // 泛型改为 DTO
        DashboardStatsDTO statsData = dashboardService.getStatsData();
        return Result.success(statsData);
    }
}
