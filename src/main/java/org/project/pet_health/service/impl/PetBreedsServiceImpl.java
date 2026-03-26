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
    public Page<PetBreeds> getAdminPage(Integer pageNum, Integer pageSize, String speciesType, String initial, String breedName) {
        LambdaQueryWrapper<PetBreeds> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(StringUtils.hasText(speciesType), PetBreeds::getSpeciesType, speciesType)
                .eq(StringUtils.hasText(initial), PetBreeds::getInitial, initial)
                .like(StringUtils.hasText(breedName), PetBreeds::getBreedName, breedName)
                // 顺便加个排序：按大类、首字母排序，让同类的品种在表格里排在一起，更美观！
                .orderByAsc(PetBreeds::getSpeciesType)
                .orderByAsc(PetBreeds::getInitial);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void batchDeleteBreeds(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }
}
