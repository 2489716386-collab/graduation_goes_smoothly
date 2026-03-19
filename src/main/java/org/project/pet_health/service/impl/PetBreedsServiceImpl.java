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
    public Page<PetBreeds> getAdminPage(Integer pageNum, Integer pageSize, Integer species, String initials) {
        LambdaQueryWrapper<PetBreeds> wrapper = new LambdaQueryWrapper<>();

        // 按大类筛选 (例如: 0猫, 1狗, 2其他)
        if (species != null) {
            wrapper.eq(PetBreeds::getSpeciesType, species);
        }
        // 模糊查询字母索引 (例如: 搜 "A" 出来阿富汗猎犬等)
        if (StringUtils.hasText(initials)) {
            wrapper.like(PetBreeds::getInitial, initials);
        }

        wrapper.orderByDesc(PetBreeds::getBreedId);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void batchDeleteBreeds(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }
}
