package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.CareKnowledgeBase;
import org.project.pet_health.service.CareKnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/care-knowledge")
@Tag(name = "PC后台-养护知识标准库")
public class CareKnowledgeBaseController {

    @Autowired
    private CareKnowledgeBaseService careService;

    @PostMapping("/add")
    @Operation(summary = "新增知识库")
    public Result add(@RequestBody CareKnowledgeBase entity) {
        careService.save(entity);
        return Result.success("新增成功");
    }

    @PutMapping("/update")
    @Operation(summary = "更新知识库")
    public Result update(@RequestBody CareKnowledgeBase entity) {
        careService.updateById(entity);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除知识库")
    public Result delete(@PathVariable Long id) {
        careService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询养护知识")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "20") Integer pageSize,
                       @RequestParam(required = false) String species) {
        Page<CareKnowledgeBase> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CareKnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(species)) {
            wrapper.like(CareKnowledgeBase::getSpecies, species); // 模糊查询
        }
        return Result.success(careService.page(page, wrapper));
    }
}
