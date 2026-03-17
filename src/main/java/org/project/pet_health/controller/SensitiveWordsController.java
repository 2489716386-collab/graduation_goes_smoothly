package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.SensitiveWords;
import org.project.pet_health.service.SensitiveWordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sensitive-words")
@Tag(name = "PC后台-敏感词管理")
public class SensitiveWordsController {

    @Autowired
    private SensitiveWordsService sensitiveWordsService;

    @PostMapping("/add")
    @Operation(summary = "新增敏感词")
    public Result add(@RequestBody SensitiveWords sensitiveWords) {
        sensitiveWordsService.save(sensitiveWords);
        return Result.success("新增成功");
    }

    @PutMapping("/update")
    @Operation(summary = "修改敏感词")
    public Result update(@RequestBody SensitiveWords sensitiveWords) {
        sensitiveWordsService.updateById(sensitiveWords);
        return Result.success("修改成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除敏感词")
    public Result delete(@PathVariable Long id) {
        sensitiveWordsService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/page")
    @Operation(summary = "分页条件查询敏感词")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String word) { // 支持按具体词汇搜索
        Page<SensitiveWords> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SensitiveWords> wrapper = new LambdaQueryWrapper<>();

        // 如果前端传了搜索关键词，则进行模糊查询
        if (StringUtils.hasText(word)) {
            wrapper.like(SensitiveWords::getWordContent, word);
        }
        // 按ID倒序排列
        wrapper.orderByDesc(SensitiveWords::getId);

        Page<SensitiveWords> result = sensitiveWordsService.page(page, wrapper);
        return Result.success(result);
    }
}
