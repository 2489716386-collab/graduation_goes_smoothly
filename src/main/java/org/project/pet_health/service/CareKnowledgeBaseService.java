package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.CareKnowledgeBaseEntity;

import java.util.List;

/**
 * <p>
 * AI 智能养护知识基准表 服务类
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
public interface CareKnowledgeBaseService extends IService<CareKnowledgeBaseEntity> {
    /**
     * 根据物种、品种和关键词模糊搜索知识库列表
     */
    List<CareKnowledgeBaseEntity> getKnowledgeList(String species, String breed, String keyword);
}
