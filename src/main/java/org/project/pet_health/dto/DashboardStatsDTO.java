package org.project.pet_health.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private Integer monthlyUsers; // 本月新增用户数
    private Integer monthlyPosts; // 本月新增动态数
}
