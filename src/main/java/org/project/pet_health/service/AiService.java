package org.project.pet_health.service;

public interface AiService {
    String getCarePlanFromAi(String systemPrompt, String userPrompt);
}
