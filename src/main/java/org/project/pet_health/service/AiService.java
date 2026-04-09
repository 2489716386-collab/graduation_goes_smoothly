package org.project.pet_health.service;

public interface AiService {
    String getCarePlanFromAi(String systemPrompt, String userPrompt);

    /**
     * 获取文本的语义向量 (Embedding)
     * @param text 输入的文本内容
     * @return 包含浮点数的向量数组
     */
    java.util.List<Double> getEmbedding(String text);
}
