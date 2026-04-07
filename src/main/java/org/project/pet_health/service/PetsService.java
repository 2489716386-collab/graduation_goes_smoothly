package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.dto.PetDTO;
import org.project.pet_health.entity.Pets;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author weiling
 * @since 2026-03-11
 */
public interface PetsService extends IService<Pets> {
    List<PetDTO> getMyPets(Long userId);
    void addMyPet(Pets pet, Long userId);
    void updateMyPet(Pets pet, Long userId);
    void deleteMyPet(Long petId, Long userId);
    // 👇 必须加上这一行：根据ID查询单个宠物详情
    PetDTO getPetDetail(Long id);
    List<PetDTO> getMyPetDTOs(Long userId);
}
