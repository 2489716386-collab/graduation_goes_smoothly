package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.HealthRecords;
import org.project.pet_health.entity.Pets;
import org.project.pet_health.mapper.HealthRecordsMapper;
import org.project.pet_health.mapper.PetsMapper;
import org.project.pet_health.service.HealthRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author weiling
 * @since 2026-03-11
 */
@Service
public class HealthRecordsServiceImpl extends ServiceImpl<HealthRecordsMapper, HealthRecords> implements HealthRecordsService {
    @Autowired
    private PetsMapper petsMapper; // 需要用到 PetsMapper 校验宠物归属

    @Override
    public List<HealthRecords> getPetHealthRecords(Long petId, Long userId) {
        // 校验这个 petId 是不是该用户的，防止非法爬取数据
        Long count = petsMapper.selectCount(new LambdaQueryWrapper<Pets>().eq(Pets::getPetId, petId).eq(Pets::getUserId, userId));
        if (count == 0) return null;

        return this.list(new LambdaQueryWrapper<HealthRecords>()
                .eq(HealthRecords::getPetId, petId));
                //.orderByDesc(HealthRecords::getRecordDate)
        // );
    }

    @Override
    public void addHealthRecord(HealthRecords record, Long userId) {
        // 同样需要先查一遍这个宠物是不是他的
        Long count = petsMapper.selectCount(new LambdaQueryWrapper<Pets>().eq(Pets::getPetId, record.getPetId()).eq(Pets::getUserId, userId));
        if (count > 0) {
            // TODO: 未来在这里接入 AI 提取报告关键字段、打分
            this.save(record);
        }
    }
}
