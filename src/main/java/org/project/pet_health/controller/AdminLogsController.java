package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.AdminAPI;
import org.project.pet_health.common.Result;
import org.project.pet_health.service.AdminLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin-logs")
@Tag(name = "PC后台-管理员操作日志")
@AdminAPI
public class AdminLogsController {

    @Autowired
    private AdminLogsService adminLogsService;

    @GetMapping("/page")
    @Operation(summary = "分页条件查询操作日志")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "20") Integer pageSize,
                       @RequestParam(required = false) Long adminId,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate) {

        return Result.success(adminLogsService.getLogsPage(pageNum, pageSize, adminId, action, startDate, endDate));
    }
}
