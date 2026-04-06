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

    // 👇 终极修改 1：全面拉长超时时间！大模型生成很慢，必须给足耐心
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 连接服务器的超时时间
            .writeTimeout(60, TimeUnit.SECONDS)   // 把 Prompt 发给大模型的超时时间
            .readTimeout(120, TimeUnit.SECONDS)   // 核心！等待大模型吐出结果的超时时间（设为 120 秒防截断）
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

            // 👇 终极修改 2：如果被 DeepSeek 拒绝，把它真正拒掉你的原因（JSON体）打印出来！
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无错误体";
                log.error("❌ AI 接口被拒绝! HTTP 状态码: {}, DeepSeek 官方报错: {}", response.code(), errorBody);
                throw new UserException("AI服务暂时不可用，请稍后再试");
            }

            // 正常解析结果
            String body = response.body().string();
            JSONObject result = JSON.parseObject(body);
            return result.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

        } catch (IOException e) {
            // 打印异常的短信息（比如 Read timed out 或 Connection refused）
            log.error("💥 AI接口网络层报错，短错误信息: {}", e.getMessage());
            // 打印完整的报错堆栈
            log.error("💥 AI接口彻底报错，完整堆栈：", e);

            throw new UserException("网络不通畅，连不上 AI 大脑");

        } catch (Exception e) {
            // 防止 JSON 解析失败导致后端悄无声息地挂掉
            log.error("💥 解析 AI 响应失败或其它异常", e);
            throw new UserException("AI 响应解析失败");
        }
    }
}
