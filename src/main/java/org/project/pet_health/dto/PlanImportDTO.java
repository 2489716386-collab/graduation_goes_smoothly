package org.project.pet_health.dto;

import lombok.Data;

@Data
public class PlanImportDTO {
    private PlanGenerateDTO snapshot; // 宠物信息的快照
    private String aiResultJson;       // AI生成的原始JSON字符串
}
