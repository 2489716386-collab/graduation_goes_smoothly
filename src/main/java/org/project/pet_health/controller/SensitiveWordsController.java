package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.entity.SensitiveWords;
import org.project.pet_health.service.SensitiveWordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sensitive-words")
@Tag(name = "PC后台-敏感词管理")
public class SensitiveWordsController {

    @Autowired
    private SensitiveWordsService sensitiveWordsService;

    @GetMapping("/admin/page")
    @Operation(summary = "分页模糊查询敏感词")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String word) {
        return Result.success(sensitiveWordsService.getAdminPage(pageNum, pageSize, word));
    }

    @PostMapping("/admin/add")
    @Operation(summary = "增加敏感词")
    @LogAction("新增了敏感词: #{#sensitiveWords.wordContent}")
    public Result add(@RequestBody SensitiveWords sensitiveWords) {
        sensitiveWordsService.save(sensitiveWords);
        return Result.success("新增敏感词成功");
    }

    @PutMapping("/admin/update")
    @Operation(summary = "修改敏感词")
    @LogAction("修改了敏感词数据, ID: #{#sensitiveWords.id}")
    public Result update(@RequestBody SensitiveWords sensitiveWords) {
        sensitiveWordsService.updateById(sensitiveWords);
        return Result.success("修改成功");
    }

    @PostMapping("/admin/delete/batch")
    @Operation(summary = "批量删除敏感词")
    @LogAction("执行了批量删除敏感词操作")
    public Result batchDelete(@RequestBody List<Long> ids) {
        // 前端提示确认后，将选中的 ID 组成数组发送到这里
        sensitiveWordsService.batchDeleteWords(ids);
        return Result.success("批量删除成功");
    }
}
