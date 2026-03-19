package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.entity.PetBreeds;
import org.project.pet_health.service.PetBreedsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pet-breeds")
@Tag(name = "PC后台-宠物品种管理")
public class PetBreedsController {

    @Autowired
    private PetBreedsService petBreedsService;

    @GetMapping("/admin/page")
    @Operation(summary = "分页条件查询宠物品种")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer species,
                       @RequestParam(required = false) String initials) {
        return Result.success(petBreedsService.getAdminPage(pageNum, pageSize, species, initials));
    }

    @PostMapping("/admin/add")
    @Operation(summary = "增加宠物品种")
    @LogAction("增加了宠物品种字典: #{#petBreeds.breedName}")
    public Result add(@RequestBody PetBreeds petBreeds) {
        petBreedsService.save(petBreeds);
        return Result.success("新增品种成功");
    }

    @PostMapping("/admin/delete/batch")
    @Operation(summary = "批量删除宠物品种")
    @LogAction("执行了批量删除宠物品种操作")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        petBreedsService.batchDeleteBreeds(ids);
        return Result.success("批量删除品种成功");
    }
}
