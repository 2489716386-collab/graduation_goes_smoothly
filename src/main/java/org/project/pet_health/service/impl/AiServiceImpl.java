package org.project.pet_health.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    public String getCarePlanFromAi(String systemPrompt, String userPrompt) {
        // 构造 OpenAI 兼容协议的请求体
        JSONObject payload = new JSONObject();
        payload.put("model", "deepseek-chat");

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().fluentPut("role", "system").fluentPut("content", systemPrompt));
        messages.add(new JSONObject().fluentPut("role", "user").fluentPut("content", userPrompt));

        payload.put("messages", messages);
        // 强制 JSON 格式输出
        payload.put("response_format", new JSONObject().fluentPut("type", "json_object"));

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(payload.toJSONString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("AI接口报错: {}", response.message());
                throw new UserException("AI服务暂时不可用，请稍后再试");
            }
            String body = response.body().string();
            JSONObject result = JSON.parseObject(body);
            return result.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        } catch (IOException e) {
            log.error("AI调用网络异常", e);
            throw new UserException("网络连接异常，生成计划失败");
        }
    }
}
