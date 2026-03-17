package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.AdminLogs;
import org.project.pet_health.service.AdminLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin-logs")
@Tag(name = "PC后台-操作日志")
public class AdminLogsController {

    @Autowired
    private AdminLogsService adminLogsService;

    @GetMapping("/page")
    @Operation(summary = "分页查询管理员操作日志")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Long adminId) {
        Page<AdminLogs> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdminLogs> wrapper = new LambdaQueryWrapper<>();

        // 可以按管理员ID精准查询他的操作记录
        if (adminId != null) {
            wrapper.eq(AdminLogs::getAdminId, adminId);
        }
        // 日志按操作时间倒序，最新的在最前面
        wrapper.orderByDesc(AdminLogs::getCreateTime);

        return Result.success(adminLogsService.page(page, wrapper));
    }
}
