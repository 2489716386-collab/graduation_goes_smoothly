package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.SensitiveWords;
import org.project.pet_health.mapper.SensitiveWordsMapper;
import org.project.pet_health.service.SensitiveWordsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class SensitiveWordsServiceImpl extends ServiceImpl<SensitiveWordsMapper, SensitiveWords> implements SensitiveWordsService {

    @Override
    public Page<SensitiveWords> getAdminPage(Integer pageNum, Integer pageSize, String word) {
        LambdaQueryWrapper<SensitiveWords> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(word)) {
            wrapper.like(SensitiveWords::getWordContent, word);
        }
        wrapper.orderByDesc(SensitiveWords::getId);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void batchDeleteWords(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids); // MyBatis-Plus 自带的批量删除
        }
    }
}
