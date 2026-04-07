package org.project.pet_health.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.project.pet_health.dto.PetDTO;
import org.project.pet_health.entity.CarePlansDayEntity;
import org.project.pet_health.entity.MoodRecords;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.mapper.MoodRecordsMapper;
import org.project.pet_health.service.AiService;
import org.project.pet_health.service.CarePlansDayService;
import org.project.pet_health.service.MoodRecordsService;
import org.project.pet_health.service.PetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 宠物心情与日记记录表 服务实现类
 * </p>
 */
@Service
@Slf4j
public class MoodRecordsServiceImpl extends ServiceImpl<MoodRecordsMapper, MoodRecords> implements MoodRecordsService {

    @Autowired
    private AiService aiService;

    @Autowired
    private CarePlansDayService carePlansDayService;

    @Autowired
    private PetsService petsService; // 需注入宠物服务获取宠物名字和品种

    @Value("${python.api-url:http://127.0.0.1:8000/predict_emotion}")
    private String pythonApiUrl;

    // 专门用于调用 Python 的 Http 客户端 (图片传输尽量快一点)
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    @Override
    public MoodRecords generateMoodDiary(Long userId, Long petId, String imageUrl) {

        // ================= 1. 请求 Python API 获取情绪 =================
        JSONObject emotionResult = getEmotionFromPython(imageUrl);

        // 校验 Python 容错逻辑 (检测到非宠物)
        if (!emotionResult.getBooleanValue("is_pet")) {
            throw new UserException("哎呀，照片里好像没有宠物哦，请重新上传！");
        }

        String moodType = emotionResult.getString("emotion");
        Double confidence = emotionResult.getDouble("confidence");


        // ================= 2. 获取业务上下文数据 =================
        // 获取宠物基础信息 (如果没有 PetsService，请根据你的项目实际情况获取)
        PetDTO pet = petsService.getPetDetail(petId);
        if (pet == null) throw new UserException("宠物不存在");

        // 获取昨天的养护打卡记录 (拼装成字符串)
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<CarePlansDayEntity> yesterdayTasks = carePlansDayService.list(
                new LambdaQueryWrapper<CarePlansDayEntity>()
                        .eq(CarePlansDayEntity::getPetId, petId)
                        // 注意：这里假设你的实体里有 taskDate 这个日期字段，如果没有请替换为实际按天查询的字段
                        .eq(CarePlansDayEntity::getPlanDate, yesterday)
        );

        // 将昨日任务转换为文本描述，例如："喂食狗粮 (已完成), 带去散步 (未完成)"
        String yesterdayCareDesc = yesterdayTasks.isEmpty() ? "昨天主人好像没有安排什么特别的计划。" :
                yesterdayTasks.stream()
                        .map(task -> task.getTaskContent() + (task.getIsCompleted() == 1 ? "(已完成)" : "(未完成)"))
                        .collect(Collectors.joining(", "));


        // ================= 3. 调用 DeepSeek 生成日记 =================
        // 注意：因为你的 AiServiceImpl 强制开启了 json_object 模式，
        // 所以 System Prompt 中【必须】包含 "JSON" 字眼，并指定 JSON 格式！
        String systemPrompt = "你现在是一只非常可爱的宠物。请你以第一人称（本宝宝、我）的口吻，写一篇50字左右的今日心情日记。" +
                "要求语气生动、萌趣、带一点小傲娇或者撒娇的感觉，多用颜文字。" +
                "【重要指令】：你必须以严格的JSON格式输出，JSON必须包含字段 'diary_content'。";

        String userPrompt = String.format(
                "我的名字叫【%s】，是一只【%s】。\n" +
                        "我刚刚拍了一张照片，AI检测到我今天的心情是：【%s】。\n" +
                        "回顾一下昨天主人的照顾情况：【%s】。\n" +
                        "请结合我的心情和昨天主人的照顾情况，生成今天的日记。如果心情好，夸夸主人；如果心情不好，傲娇地吐槽一下昨天的未完成项。",
                pet.getName(), pet.getBreedName(), moodType, yesterdayCareDesc
        );

        String aiResponseJsonStr = aiService.getCarePlanFromAi(systemPrompt, userPrompt);

        // 解析 DeepSeek 返回的 JSON，取出日记内容
        String diaryContent = "今天也是充满希望的一天呀～"; // 兜底文本
        try {
            JSONObject aiResJson = JSON.parseObject(aiResponseJsonStr);
            if(aiResJson.containsKey("diary_content")) {
                diaryContent = aiResJson.getString("diary_content");
            }
        } catch (Exception e) {
            log.error("解析DeepSeek日记JSON失败, 原始返回: {}", aiResponseJsonStr, e);
        }


        // ================= 4. 落库保存 =================
        MoodRecords record = new MoodRecords();
        record.setUserId(userId);
        record.setPetId(petId);
        record.setRecordDate(LocalDate.now()); // 今日打卡
        record.setImageUrl(imageUrl);
        record.setMoodType(moodType);
        record.setConfidence(confidence);
        record.setDiaryContent(diaryContent);
        record.setIsShared(0); // 默认未分享

        this.save(record);

        return record;
    }


    /**
     * 封装调用 Python 接口的私有方法：处理云端图片下载和 Multipart 转发
     */
    private JSONObject getEmotionFromPython(String imageUrl) {
        try {
            // 1. 从 uniCloud (或云存储) 下载图片流
            Request downloadReq = new Request.Builder().url(imageUrl).build();
            try (Response downloadRes = httpClient.newCall(downloadReq).execute()) {

                if (!downloadRes.isSuccessful() || downloadRes.body() == null) {
                    throw new UserException("无法读取云端图片");
                }
                byte[] imageBytes = downloadRes.body().bytes(); // 拿到图片的二进制流

                // 2. 伪装成前端表单上传的形式，把二进制流发给 Python
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        // 参数名 "file" 必须和 Python fastapi 定义的参数名一致
                        .addFormDataPart("file", "pet_image.jpg",
                                RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                        .build();

                Request pythonReq = new Request.Builder()
                        .url(pythonApiUrl)
                        .post(requestBody)
                        .build();

                // 3. 执行 Python 请求并解析结果
                try (Response pythonRes = httpClient.newCall(pythonReq).execute()) {
                    if (!pythonRes.isSuccessful() || pythonRes.body() == null) {
                        throw new UserException("Python AI 服务异常");
                    }
                    String resStr = pythonRes.body().string();
                    log.info("Python 返回结果: {}", resStr);

                    return JSON.parseObject(resStr);
                }
            }
        } catch (IOException e) {
            log.error("调用 Python 情绪识别接口失败", e);
            throw new UserException("心情检测失败，请检查 AI 服务是否开启");
        }
    }
}
