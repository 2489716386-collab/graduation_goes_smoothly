package org.project.pet_health.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetDTO {
    //
    @JsonProperty("petid")
    private Long petid;
    private String name;
    private String avatar;
    private String breedName; // 关联查询出的品种名
    private Integer age;      // 根据 birthDate 计算出的年龄
    private Double weight;
    private LocalDate birthDate; // 生日
    private Integer gender;
}
