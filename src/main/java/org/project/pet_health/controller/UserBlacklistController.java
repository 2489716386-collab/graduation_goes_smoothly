package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.UserBlacklist;
import org.project.pet_health.service.UserBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-blacklist")
@Tag(name = "PC后台-用户黑名单管理")
public class UserBlacklistController {

    @Autowired
    private UserBlacklistService userBlacklistService;

    @PostMapping("/add")
    @Operation(summary = "拉黑用户(新增黑名单记录)")
    public Result add(@RequestBody UserBlacklist userBlacklist) {
        // 实际业务中，这里除了往黑名单表插数据，往往还需要去 Users 表里把该用户的 status 置为 0（封禁状态）
        userBlacklistService.save(userBlacklist);
        return Result.success("成功将用户加入黑名单");
    }

    @PutMapping("/update")
    @Operation(summary = "修改黑名单记录(如修改封禁理由或解封时间)")
    public Result update(@RequestBody UserBlacklist userBlacklist) {
        userBlacklistService.updateById(userBlacklist);
        return Result.success("修改黑名单记录成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "解除拉黑(移出黑名单)")
    public Result delete(@PathVariable Long id) {
        // 实际业务中，移出黑名单后，也可同步将 Users 表里的用户 status 恢复为 1（正常）
        userBlacklistService.removeById(id);
        return Result.success("解除拉黑成功");
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询黑名单列表(支持按用户ID筛选)")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Long userId) {
        Page<UserBlacklist> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserBlacklist> wrapper = new LambdaQueryWrapper<>();

        // 如果管理员输入了特定的用户ID，则精准查询该用户的黑名单记录
        if (userId != null) {
            wrapper.eq(UserBlacklist::getUserId, userId);
        }

        // 按拉黑时间倒序排列，最新被拉黑的展示在前面
        wrapper.orderByDesc(UserBlacklist::getCreateTime);

        Page<UserBlacklist> result = userBlacklistService.page(page, wrapper);
        return Result.success(result);
    }
}
