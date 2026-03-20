package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.Pets;
import org.project.pet_health.service.PetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pets")
@Tag(name = "小程序-我的宠物档案")
public class PetsController {

    @Autowired
    private PetsService petsService;

    @GetMapping("/user/list")
    @Operation(summary = "获取我的所有宠物列表")
    public Result myPets(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(petsService.getMyPets(userId));
    }

    @PostMapping("/user/add")
    @Operation(summary = "新增宠物档案")
    public Result addPet(@RequestBody Pets pet, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        petsService.addMyPet(pet, userId);
        return Result.success("宠物添加成功");
    }

    @PutMapping("/user/update")
    @Operation(summary = "修改宠物档案")
    public Result updatePet(@RequestBody Pets pet, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        petsService.updateMyPet(pet, userId);
        return Result.success("宠物资料已更新");
    }

    @DeleteMapping("/user/delete/{petId}")
    @Operation(summary = "删除宠物")
    public Result deletePet(@PathVariable Long petId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        petsService.deleteMyPet(petId, userId);
        return Result.success("宠物已删除");
    }
}
