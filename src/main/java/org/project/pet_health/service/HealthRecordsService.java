package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.HealthRecords;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author weiling
 * @since 2026-03-11
 */
public interface HealthRecordsService extends IService<HealthRecords> {
    List<HealthRecords> getPetHealthRecords(Long petId, Long userId);
    void addHealthRecord(HealthRecords record, Long userId);
}
