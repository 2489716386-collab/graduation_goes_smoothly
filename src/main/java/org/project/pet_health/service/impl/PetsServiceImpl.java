package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.Pets;
import org.project.pet_health.mapper.PetsMapper;
import org.project.pet_health.service.PetsService;
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
public class PetsServiceImpl extends ServiceImpl<PetsMapper, Pets> implements PetsService {

    @Override
    public List<Pets> getMyPets(Long userId) {
        return this.list(new LambdaQueryWrapper<Pets>().eq(Pets::getUserId, userId).orderByDesc(Pets::getCreateTime));
    }

    @Override
    public void addMyPet(Pets pet, Long userId) {
        pet.setUserId(userId);
        this.save(pet);
    }

    @Override
    public void updateMyPet(Pets pet, Long userId) {
        // 使用 LambdaUpdateWrapper 确保只能修改属于自己的宠物
        this.update(pet, new LambdaUpdateWrapper<Pets>()
                .eq(Pets::getPetId, pet.getPetId())
                .eq(Pets::getUserId, userId));
    }

    @Override
    public void deleteMyPet(Long petId, Long userId) {
        this.remove(new LambdaQueryWrapper<Pets>()
                .eq(Pets::getPetId, petId)
                .eq(Pets::getUserId, userId));
    }
}
