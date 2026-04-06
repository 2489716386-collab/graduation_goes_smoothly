package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.dto.PetDTO;
import org.project.pet_health.entity.PetBreeds;
import org.project.pet_health.entity.Pets;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.mapper.PetsMapper;
import org.project.pet_health.service.PetBreedsService;
import org.project.pet_health.service.PetsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        List<Pets> list = this.list(new LambdaQueryWrapper<Pets>().eq(Pets::getUserId, userId));

        return list.stream().map(pet -> {
            PetDTO dto = new PetDTO();
            BeanUtils.copyProperties(pet, dto);

            // 【关键点】：手动强行赋值 ID，防止 BeanUtils 漏掉
            dto.setPetid(pet.getPetId());

            // 处理品种
            if (pet.getBreedId() != null) {
                PetBreeds breed = petBreedsService.getById(pet.getBreedId());
                dto.setBreedName(breed != null ? breed.getBreedName() : "未知");
            }

            // 处理年龄
            if (pet.getBirthDate() != null) {
                dto.setAge(LocalDate.now().getYear() - pet.getBirthDate().getYear());
            } else {
                dto.setAge(0);
            }
            return dto;
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

    // 👇 把这段实现方法粘贴到 PetsServiceImpl 类里面
    @Override
    public PetDTO getPetDetail(Long id) {
        // 1. 从数据库查询原始宠物实体
        Pets pet = this.getById(id);
        if (pet == null) {
            throw new UserException("未找到该宠物信息");
        }

        // 2. 转换为 DTO，准备传给前端
        PetDTO dto = new PetDTO();
        BeanUtils.copyProperties(pet, dto);

        // 【关键防御】：强制手动赋值一次 ID，防止大小写问题导致前端拿到 null
        dto.setPetid(pet.getPetId());

        // 3. 关联查询品种名称
        if (pet.getBreedId() != null) {
            PetBreeds breed = petBreedsService.getById(pet.getBreedId());
            dto.setBreedName(breed != null ? breed.getBreedName() : "未知品种");
        } else {
            dto.setBreedName("未知品种");
        }

        // 4. 实时计算年龄
        if (pet.getBirthDate() != null) {
            int age = LocalDate.now().getYear() - pet.getBirthDate().getYear();
            dto.setAge(Math.max(0, age)); // 确保年龄不为负数
        } else {
            dto.setAge(0);
        }

        return dto;
    }
}
