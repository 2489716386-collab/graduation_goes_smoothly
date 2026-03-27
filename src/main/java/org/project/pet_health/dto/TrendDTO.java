package org.project.pet_health.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrendDTO {
    private List<String> dates;       // X轴的日期列表，如 ["03-20", "03-21", ...]
    private List<Integer> userCounts; // 新增用户数数组
    private List<Integer> postCounts; // 新增动态数数组
}
