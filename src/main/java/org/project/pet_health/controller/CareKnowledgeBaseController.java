// src/main/java/org/project/pet_health/controller/CareKnowledgeBaseController.java
package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.CareKnowledgeBaseEntity;
import org.project.pet_health.service.CareKnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/knowledge-base")
@Tag(name = "宠物养护知识库接口")
public class CareKnowledgeBaseController {

    @Autowired
    private CareKnowledgeBaseService knowledgeBaseService;

    @GetMapping("/list")
    @Operation(summary = "条件筛选并获取知识库内容")
    public Result list(
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String keyword) {

        // 业务逻辑全部委托给 Service 层
        return Result.success(knowledgeBaseService.getKnowledgeList(species, breed, keyword));
    }

    @PostMapping("/add")
    @Operation(summary = "新增知识库条目")
    public Result addKnowledge(@RequestBody CareKnowledgeBaseEntity entity) {
        // 直接使用 MyBatis-Plus 提供的 Service 方法
        knowledgeBaseService.save(entity);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    @Operation(summary = "修改知识库条目")
    public Result updateKnowledge(@RequestBody CareKnowledgeBaseEntity entity) {
        knowledgeBaseService.updateById(entity);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除知识库条目")
    public Result deleteKnowledge(@PathVariable Long id) {
        knowledgeBaseService.removeById(id);
        return Result.success("删除成功");
    }
}
