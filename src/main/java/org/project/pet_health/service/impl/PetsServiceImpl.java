package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.dto.PetDTO;
import org.project.pet_health.entity.PetBreeds;
import org.project.pet_health.entity.Pets;
import org.project.pet_health.mapper.PetsMapper;
import org.project.pet_health.service.PetBreedsService;
import org.project.pet_health.service.PetsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private PetBreedsService petBreedsService;

    @Override
    public List<PetDTO> getMyPets(Long userId) {

        // 1. 获取原始宠物列表
        List<Pets> petsList = this.list(new LambdaQueryWrapper<Pets>()
                .eq(Pets::getUserId, userId)
                .orderByDesc(Pets::getCreateTime));

        // 2. 转换为 VO 并填充额外信息
        return petsList.stream().map(pet -> {
            PetDTO vo = new PetDTO();
            BeanUtils.copyProperties(pet, vo);

            // 关键点 1：计算年龄 (当前日期 - 出生日期)
            if (pet.getBirthDate() != null) {
                int age = Period.between(pet.getBirthDate(), LocalDate.now()).getYears();
                vo.setAge(Math.max(0, age));
            }

            // 关键点 2：关联查询品种名称
            if (pet.getBreedId() != null) {
                PetBreeds breed = petBreedsService.getById(pet.getBreedId());
                vo.setBreedName(breed != null ? breed.getBreedName() : "未知品种");
            }

            return vo;
        }).collect(Collectors.toList());
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
