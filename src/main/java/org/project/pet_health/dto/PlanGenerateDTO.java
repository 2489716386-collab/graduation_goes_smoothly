package org.project.pet_health.dto;

import lombok.Data;

@Data
public class PlanGenerateDTO {
    private Long petId;
    private String breed;
    private String age;
    private Double weight;
    private String healthStatus;
    private String feedback; // 重新生成时的反馈意见
}
