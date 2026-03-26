package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.PetBreeds;
import org.project.pet_health.mapper.PetBreedsMapper;
import org.project.pet_health.service.PetBreedsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class PetBreedsServiceImpl extends ServiceImpl<PetBreedsMapper, PetBreeds> implements PetBreedsService {

    @Override
    public Page<PetBreeds> getAdminPage(Integer pageNum, Integer pageSize, String species, String initialLetter, String breedName) {
        LambdaQueryWrapper<PetBreeds> wrapper = new LambdaQueryWrapper<>();

// 如果传了 "猫"，就按 species_type 精确查询
        wrapper.eq(StringUtils.hasText(species), PetBreeds::getSpeciesType, species)
                // 如果传了 "B"，就按 initial 精确查询
                .eq(StringUtils.hasText(initialLetter), PetBreeds::getInitial, initialLetter)
                // 如果传了 "布偶"，就按 breed_name 模糊查询
                .like(StringUtils.hasText(breedName), PetBreeds::getBreedName, breedName);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void batchDeleteBreeds(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }
}
