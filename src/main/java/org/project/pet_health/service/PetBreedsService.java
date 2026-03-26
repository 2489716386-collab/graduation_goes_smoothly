package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.PetBreeds;
import java.util.List;

public interface PetBreedsService extends IService<PetBreeds> {
    Page<PetBreeds> getAdminPage(Integer pageNum, Integer pageSize, String species, String initialLetter, String breedName);
    void batchDeleteBreeds(List<Integer> ids);
}
