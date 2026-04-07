package org.project.pet_health.service;

import org.project.pet_health.entity.MoodRecords;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MoodRecordsService extends IService<MoodRecords> {

    /**
     * 检测心情并生成 AI 宠物日记
     * @param userId 当前用户ID
     * @param petId 宠物ID
     * @param imageUrl 前端上传后返回的云端图片URL
     * @return 包含日记内容和识别结果的实体类
     */
    MoodRecords generateMoodDiary(Long userId, Long petId, String imageUrl);
}
