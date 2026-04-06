package org.project.pet_health.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.dto.PlanGenerateDTO;
import org.project.pet_health.dto.PlanImportDTO;
import org.project.pet_health.entity.CareKnowledgeBaseEntity;
import org.project.pet_health.entity.CarePlansDayEntity;
import org.project.pet_health.entity.CarePlansWeekEntity;
import org.project.pet_health.mapper.CareKnowledgeBaseMapper;
import org.project.pet_health.mapper.CarePlansWeekMapper;
import org.project.pet_health.service.AiService;
import org.project.pet_health.service.CarePlansDayService;
import org.project.pet_health.service.CarePlansWeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * AI养护周计划主表 服务实现类
 * </p>
 *
 * @author weiling
 * @since 2026-04-06
 */
// service/impl/CarePlansWeekServiceImpl.java
@Service
public class CarePlansWeekServiceImpl extends ServiceImpl<CarePlansWeekMapper, CarePlansWeekEntity> implements CarePlansWeekService {

    @Autowired
    private AiService aiService;

    @Autowired
    private CareKnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private CarePlansDayService dayService;

    @Override
    public String generatePreview(PlanGenerateDTO dto) {
        // 1. 检索知识库（参照你 Entity 的 species, breed 字段）
        CareKnowledgeBaseEntity advice = knowledgeBaseMapper.selectOne(
                new LambdaQueryWrapper<CareKnowledgeBaseEntity>()
                        .eq(CareKnowledgeBaseEntity::getBreed, dto.getBreed())
                        .last("LIMIT 1")
        );

        // 2. 构造 AI 提示词
        String systemPrompt = "你是一位专业的执业兽医师。请根据宠物资料和知识库规范生成一份详细的周养护计划。\n" +
                "你必须返回合法的 JSON 格式，结构如下：\n" +
                "{\n" +
                "  \"weekly_focus\": \"本周养护核心建议\",\n" +
                "  \"daily_tasks\": [{\"category\": \"饮食\", \"content\": \"任务描述\"}]\n" +
                "}";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append(String.format("宠物信息：品种-%s，年龄-%s，体重-%skg，健康现状-%s。",
                dto.getBreed(), dto.getAge(), dto.getWeight(), dto.getHealthStatus()));

        if (advice != null) {
            userPrompt.append("\n【知识库参考建议】：").append(advice.getDietAdvice())
                    .append("\n【禁忌】：").append(advice.getCaution());
        }

        if (StringUtils.hasText(dto.getFeedback())) {
            userPrompt.append("\n【用户调整反馈】：").append(dto.getFeedback());
        }

        // 3. 调用 AI 实现类
        return aiService.getCarePlanFromAi(systemPrompt, userPrompt.toString());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 确保数据一致性
    public void confirmAndImport(PlanImportDTO importDto) {
        PlanGenerateDTO snap = importDto.getSnapshot();

        // 1. 将该宠物原有的周计划设为历史记录 (is_current = 0)
        this.update(new LambdaUpdateWrapper<CarePlansWeekEntity>()
                .eq(CarePlansWeekEntity::getPetId, snap.getPetId())
                .set(CarePlansWeekEntity::getIsCurrent, 0));

        // 2. 保存新的周计划快照
        JSONObject aiJson = JSON.parseObject(importDto.getAiResultJson());
        CarePlansWeekEntity weekPlan = new CarePlansWeekEntity();
        weekPlan.setPetId(snap.getPetId());
        weekPlan.setSnapshotAge(snap.getAge());
        weekPlan.setSnapshotWeight(BigDecimal.valueOf(snap.getWeight()));
        weekPlan.setSnapshotHealthStatus(snap.getHealthStatus());
        weekPlan.setWeeklyFocus(aiJson.getString("weekly_focus"));
        weekPlan.setStartDate(LocalDate.now());
        weekPlan.setEndDate(LocalDate.now().plusDays(6));
        weekPlan.setIsCurrent(true);
        this.save(weekPlan);

        // 3. 任务裂变：生成未来 7 天的每日打卡任务
        JSONArray dailyTasks = aiJson.getJSONArray("daily_tasks");
        List<CarePlansDayEntity> dayEntities = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            for (int j = 0; j < dailyTasks.size(); j++) {
                JSONObject task = dailyTasks.getJSONObject(j);
                CarePlansDayEntity day = new CarePlansDayEntity();
                day.setWeekPlanId(weekPlan.getId());
                day.setPetId(snap.getPetId());
                day.setPlanDate(date);
                day.setDayOfWeek(date.getDayOfWeek().getValue());
                day.setTaskCategory(task.getString("category"));
                day.setTaskContent(task.getString("content"));
                day.setIsCompleted(false);
                dayEntities.add(day);
            }
        }
        dayService.saveBatch(dayEntities);
    }
}
