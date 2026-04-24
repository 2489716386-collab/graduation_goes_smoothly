package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.CareKnowledgeBaseEntity;
import org.project.pet_health.mapper.CareKnowledgeBaseMapper;
import org.project.pet_health.service.CareKnowledgeBaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * AI 智能养护知识基准表 服务实现类
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
@Service
public class CareKnowledgeBaseServiceImpl extends ServiceImpl<CareKnowledgeBaseMapper, CareKnowledgeBaseEntity> implements CareKnowledgeBaseService {

    @Override
    public List<CareKnowledgeBaseEntity> getKnowledgeList(String species, String breed, String keyword) {
        LambdaQueryWrapper<CareKnowledgeBaseEntity> wrapper = new LambdaQueryWrapper<>();

        // 1. 物种和品种精确筛选
        wrapper.eq(StringUtils.hasText(species), CareKnowledgeBaseEntity::getSpecies, species)
                .eq(StringUtils.hasText(breed), CareKnowledgeBaseEntity::getBreed, breed);

        // 2. 文本内容多字段模糊搜索 (使用 OR 连接)
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CareKnowledgeBaseEntity::getDietAdvice, keyword)
                    .or().like(CareKnowledgeBaseEntity::getHygieneAdvice, keyword)
                    .or().like(CareKnowledgeBaseEntity::getMedicalAdvice, keyword)
                    .or().like(CareKnowledgeBaseEntity::getEnvironmentAdvice, keyword)
                    .or().like(CareKnowledgeBaseEntity::getCaution, keyword));
        }

        // 3. 可以在这里统一加上排序逻辑，比如按 ID 降序
        wrapper.orderByDesc(CareKnowledgeBaseEntity::getId);

        return this.list(wrapper);
    }

}
