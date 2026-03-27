package org.project.pet_health.service;

import org.project.pet_health.dto.DashboardStatsDTO;
import org.project.pet_health.dto.TrendDTO;

public interface DashboardService  {

    TrendDTO getTrendData();

    DashboardStatsDTO getStatsData();

}
