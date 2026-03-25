package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.service.UserBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-blacklist")
@Tag(name = "PC后台-黑名单管理")
public class UserBlacklistController {

    @Autowired
    private UserBlacklistService userBlacklistService;

    @GetMapping("/page")
    @Operation(summary = "分页模糊查询黑名单")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String nickname,
                       @RequestParam(required = false) Long userId,
                       @RequestParam(required = false) String createDate) {
        return Result.success(userBlacklistService.getBlacklistPage(pageNum, pageSize, nickname, userId, createDate));    }

    @PostMapping("/unban/batch")
    @Operation(summary = "批量解封用户(移出黑名单)")
    @LogAction("解封了用户，用户ID: #{#userIds}")
    public Result batchUnban(@RequestBody List<Long> userIds) {
        // 前端通过勾选复选框，传递类似于 [101, 105, 108] 这样的用户ID数组过来
        userBlacklistService.batchUnbanUsers(userIds);
        return Result.success("选中用户已成功解封！");
    }
}
