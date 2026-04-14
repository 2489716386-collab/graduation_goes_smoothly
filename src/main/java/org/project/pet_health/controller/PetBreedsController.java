package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.AdminAPI;
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
                       @RequestParam(defaultValue = "20") Integer pageSize,
                       @RequestParam(required = false) String speciesType,
                       @RequestParam(required = false) String breedName) {
        return Result.success(petBreedsService.getAdminPage(pageNum, pageSize, speciesType, breedName));
    }

    @PostMapping("/admin/add")
    @Operation(summary = "增加宠物品种")
    @LogAction("增加了宠物品种字典: #{#petBreeds.breedName}")
    @AdminAPI
    public Result add(@RequestBody PetBreeds petBreeds) {
        petBreedsService.save(petBreeds);
        return Result.success("新增品种成功");
    }

    @PostMapping("/admin/delete")
    @Operation(summary = "批量删除宠物品种")
    @LogAction("执行了批量删除宠物品种操作")
    @AdminAPI
    public Result batchDelete(@RequestBody List<Integer> ids) {
        petBreedsService.removeByIds(ids);
        return Result.success("批量删除品种成功");
    }

    @PutMapping("/admin/update")
    @Operation(summary = "修改宠物品种")
    @LogAction("修改了宠物品种字典: #{#petBreeds.breedName}")
    @AdminAPI
    public Result update(@RequestBody PetBreeds petBreeds) {
        petBreedsService.updateById(petBreeds);
        return Result.success("修改品种成功");
    }

    // === 补充：给小程序端提供的获取所有品种列表接口 ===
    @GetMapping("/list")
    @Operation(summary = "获取所有宠物品种列表(供小程序下拉框使用)")
    public Result list() {
        // 使用 MyBatis-Plus 自带的 list() 方法，直接查出所有品种
        return Result.success(petBreedsService.list());
    }
}
