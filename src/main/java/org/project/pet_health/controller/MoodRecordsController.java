package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.MoodRecords;
import org.project.pet_health.service.MoodRecordsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 宠物心情日志 前端控制器
 * </p>
 *
 * @author weiling
 * @since 2026-04-07
 */
@RestController
@RequestMapping("/mood-records")
@Tag(name = "宠物心情与AI日记接口")
@RequiredArgsConstructor // 使用构造器注入，比@Autowired更优雅
public class MoodRecordsController {

    private final MoodRecordsService moodRecordsService;

    /**
     * 核心接口：上传图片后调用此接口生成AI日记
     * 前端流程：1. uniCloud.uploadFile -> 2. 拿到url -> 3. 调用本接口
     */
    @PostMapping("/generate")
    @Operation(summary = "【核心】检测心情并生成AI宠物日记", description = "传入上传后的图片URL，系统自动完成识别、数据整合及AI生成")
    public Result<MoodRecords> generateDiary(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "宠物ID") @RequestParam Long petId,
            @Parameter(description = "uniCloud返回的图片URL") @RequestParam String imageUrl) {

        // 调用我们刚才写的 Service 逻辑
        MoodRecords record = moodRecordsService.generateMoodDiary(userId, petId, imageUrl);

        return Result.success(record);
    }

    /**
     * 获取宠物当天的日记（用于首页显示）
     */
    @GetMapping("/today/{petId}")
    @Operation(summary = "获取宠物今日心情日记", description = "如果今日已生成，则返回内容；否则返回空")
    public Result<MoodRecords> getTodayDiary(@PathVariable Long petId) {
        // 使用 MyBatis-Plus 的查询方式
        MoodRecords todayRecord = moodRecordsService.lambdaQuery()
                .eq(MoodRecords::getPetId, petId)
                .eq(MoodRecords::getRecordDate, java.time.LocalDate.now())
                .orderByDesc(MoodRecords::getCreateTime)
                .last("limit 1")
                .one();

        return Result.success(todayRecord);
    }

    /**
     * 查看历史日志（点击“小书”进入的页面）
     */
    @GetMapping("/history/{petId}")
    @Operation(summary = "查看宠物日志列表", description = "按时间倒序排列，查看历史心情和日记")
    public Result<List<MoodRecords>> getMoodHistory(@PathVariable Long petId) {
        List<MoodRecords> history = moodRecordsService.lambdaQuery()
                .eq(MoodRecords::getPetId, petId)
                .orderByDesc(MoodRecords::getRecordDate)
                .list();

        return Result.success(history);
    }

    /**
     * 分享到社区（修改状态）
     */
    @PutMapping("/share/{moodId}")
    @Operation(summary = "将日记分享到社区", description = "修改分享状态，后续可联动社区发帖逻辑")
    public Result<String> shareToCommunity(@PathVariable Long moodId) {
        boolean updated = moodRecordsService.lambdaUpdate()
                .set(MoodRecords::getIsShared, 1)
                .eq(MoodRecords::getMoodId, moodId)
                .update();

        return updated ? Result.success("分享成功") : Result.error("操作失败");
    }
}
