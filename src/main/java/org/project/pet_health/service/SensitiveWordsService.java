package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.SensitiveWords;
import java.util.List;

public interface SensitiveWordsService extends IService<SensitiveWords> {
    Page<SensitiveWords> getAdminPage(Integer pageNum, Integer pageSize, String word);
    void batchDeleteWords(List<Long> ids);
}
