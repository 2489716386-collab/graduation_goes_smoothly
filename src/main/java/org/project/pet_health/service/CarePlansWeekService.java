package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.dto.PlanGenerateDTO;
import org.project.pet_health.dto.PlanImportDTO;
import org.project.pet_health.entity.CarePlansWeekEntity;

/**
 * <p>
 * AI养护周计划主表 服务类
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
public interface CarePlansWeekService extends IService<CarePlansWeekEntity> {
    String generatePreview(PlanGenerateDTO dto);
    void confirmAndImport(PlanImportDTO importDto);

    /**
     * 获取宠物本周有效的养护计划
     * @param petId 宠物ID
     * @return CarePlansWeekEntity 如果本周尚未生成，则返回 null
     */
    CarePlansWeekEntity getActivePlan(Long petId);
}
