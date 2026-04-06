package org.project.pet_health.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.project.pet_health.dto.PlanGenerateDTO;
import org.project.pet_health.dto.PlanImportDTO;
import org.project.pet_health.entity.*;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.mapper.CareKnowledgeBaseMapper;
import org.project.pet_health.mapper.CarePlansWeekMapper;
import org.project.pet_health.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
@Slf4j
public class CarePlansWeekServiceImpl extends ServiceImpl<CarePlansWeekMapper, CarePlansWeekEntity> implements CarePlansWeekService {

    @Autowired
    private AiService aiService;

    @Autowired
    private CareKnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private CarePlansDayService dayService;

    @Autowired
    private PetsService petsService;

    @Autowired
    private PetBreedsService breedsService;

    @Override
    public String generatePreview(PlanGenerateDTO dto) {
        // 1. 获取宠物基础信息（从 pets 表）
        Pets pet = petsService.getById(dto.getPetId());
        if (pet == null) {
            throw new UserException("未找到对应的宠物信息");
        }

        // 2. 转换品种 ID 为名称
        String breedName = "普通宠物";
        if (pet.getBreedId() != null) {
            PetBreeds breed = breedsService.getById(pet.getBreedId());
            if (breed != null) {
                breedName = breed.getBreedName();
            }
        }

        // 3. 计算年龄（根据 birthDate 实时计算）
        int age = 0;
        if (pet.getBirthDate() != null) {
            age = LocalDate.now().getYear() - pet.getBirthDate().getYear();
            age = Math.max(0, age); // 确保年龄不为负数
        }

        // 计算今天到本周日的日期范围
        LocalDate today = LocalDate.now();
        LocalDate nextSunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));// 找到本周的周日

        // 4. 从知识库检索该品种的养护标准建议
        CareKnowledgeBaseEntity advice = knowledgeBaseMapper.selectOne(
                new LambdaQueryWrapper<CareKnowledgeBaseEntity>()
                        .eq(CareKnowledgeBaseEntity::getBreed, breedName) // 按名称匹配知识库
                        .last("LIMIT 1")
        );


        // 5. 构造系统级提示词 (System Prompt)
        String systemPrompt = "你是一位专业的执业兽医师。请根据宠物资料和知识库规范，生成一份从 " + today + " 至 " + nextSunday + " 的周养护计划。" +
                "你必须返回合法的 JSON 格式，结构必须严格如下：\n" +
                "{\n" +
                "  \"weekly_focus\": \"本周养护核心建议\",\n" +
                "  \"daily_tasks\": [\n" +
                "    {\"date\": \"YYYY-MM-DD\", \"category\": \"饮食/清洁/运动\", \"content\": \"具体的任务描述\"}\n" +
                "  ]\n" +
                "}。\n" +
                "注意：\n" +
                "1. 请仅生成从 " + today + " 到 " + nextSunday + " 每天的任务，不要生成其他日期的任务。\n" +
                "2. 每天可以包含多条不同 category 的任务，确保覆盖宠物近况中提到的问题。";

        // 6. 构造用户提示词 (User Prompt) - 使用查询到的真实数据
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append(String.format("宠物信息：品种-%s，年龄-%d岁，体重-%skg，健康现状-%s。",
                breedName, age, pet.getWeight(), dto.getHealthStatus()));

        if (advice != null) {
            userPrompt.append("\n【医学参考建议】：").append(advice.getDietAdvice())
                    .append("\n【禁忌事项】：").append(advice.getCaution());
        }

        // 如果用户提交了“不满意”的反馈，追加到 Prompt 尾部
        if (StringUtils.hasText(dto.getFeedback())) {
            userPrompt.append("\n【用户调整反馈】：").append(dto.getFeedback());
        }

        // 7. 调用 AI 实现类并返回结果
        log.info("正在为宠物 {} 生成 AI 养护计划预览...", pet.getName());
        return aiService.getCarePlanFromAi(systemPrompt, userPrompt.toString());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAndImport(PlanImportDTO dto) {
        // 【关键点】从 dto 中取出嵌套的 snapshot 对象
        PlanGenerateDTO snapshot = dto.getSnapshot();
        Long petId = snapshot.getPetId();

        // 先查出这只宠物，主要为了拿 user_id，顺便更新体重
        Pets pet = petsService.getById(petId);
        if (pet == null) {
            throw new UserException("宠物不存在");
        }

        // 将前端新输入的体重同步更新到宠物档案
        pet.setWeight(snapshot.getWeight());
        petsService.updateById(pet); // 更新 pets 表

        // --- ② 将该宠物原有的周计划设为历史记录 ---
        this.update(new LambdaUpdateWrapper<CarePlansWeekEntity>()
                .eq(CarePlansWeekEntity::getPetId, petId)
                .set(CarePlansWeekEntity::getIsCurrent, 0));

        // --- ③ 保存新的周计划快照 ---
        JSONObject aiJson = JSON.parseObject(dto.getAiResultJson());
        CarePlansWeekEntity weekPlan = new CarePlansWeekEntity();
        weekPlan.setPetId(petId);
        weekPlan.setUserId(pet.getUserId()); // 确保拿到主人的ID

        // 记录生成计划时的“瞬时状态”
        weekPlan.setSnapshotAge(snapshot.getAge());
        weekPlan.setSnapshotWeight(BigDecimal.valueOf(snapshot.getWeight()));
        weekPlan.setSnapshotHealthStatus(snapshot.getHealthStatus());
        weekPlan.setWeeklyFocus(aiJson.getString("weekly_focus"));

        // 👇 【核心修改 1】：动态计算这周日的日期
        LocalDate today = LocalDate.now();
        LocalDate thisSunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        weekPlan.setStartDate(today);
        weekPlan.setEndDate(thisSunday); // 结束日期固定为本周日
        weekPlan.setIsCurrent(true);     // 设为当前生效计划
        this.save(weekPlan);             // 保存主表，MyBatis-Plus 会自动将生成的 ID 回填给 weekPlan

        // --- ④ 任务裂变：直接读取 AI 返回的日期并保存 ---
        JSONArray dailyTasks = aiJson.getJSONArray("daily_tasks");
        List<CarePlansDayEntity> dayList = new ArrayList<>();

        if (dailyTasks != null) {
            // 👇 【核心修改 2】：去掉双重嵌套循环！直接遍历 AI 返回的任务数组即可
            for (int i = 0; i < dailyTasks.size(); i++) {
                JSONObject taskObj = dailyTasks.getJSONObject(i);
                CarePlansDayEntity dayTask = new CarePlansDayEntity();

                dayTask.setWeekPlanId(weekPlan.getId()); // 关联刚才生成的周计划主键 ID
                dayTask.setPetId(petId);

                // 直接解析大模型在 JSON 里返回的 "date" 字段 (格式为 YYYY-MM-DD)
                LocalDate taskDate = LocalDate.parse(taskObj.getString("date"));
                dayTask.setPlanDate(taskDate);
                dayTask.setDayOfWeek(taskDate.getDayOfWeek().getValue());

                dayTask.setTaskCategory(taskObj.getString("category"));
                dayTask.setTaskContent(taskObj.getString("content"));
                dayTask.setIsCompleted(0); // 0代表未打卡

                dayList.add(dayTask);
            }
        }
        // 批量保存这些每日任务到数据库
        dayService.saveBatch(dayList);
    }
}
