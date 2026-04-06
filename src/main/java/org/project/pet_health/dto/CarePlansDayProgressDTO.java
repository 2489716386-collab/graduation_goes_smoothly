package org.project.pet_health.dto;

import lombok.Data;
import org.project.pet_health.entity.CarePlansDayEntity;

import java.util.List;

@Data
public class CarePlansDayProgressDTO {
    private List<CarePlansDayEntity> tasks; // 今日任务列表
    private int progress;                   // 今日打卡进度百分比 (0-100)
}
