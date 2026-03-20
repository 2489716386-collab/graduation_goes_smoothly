package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@Tag(name = "PC后台-举报记录管理")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @GetMapping("/admin/page")
    @Operation(summary = "分页条件查询举报记录")
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) Integer targetType,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(required = false) String reason,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {
        return Result.success(reportsService.getAdminPage(pageNum, pageSize, targetType, status, reason, startDate, endDate));
    }

    @PostMapping("/user/add")
    @Operation(summary = "【用户端】新增举报记录")
    // 这里是用户操作，不加 @LogAction 管理员日志
    public Result addUserReport(@RequestBody Reports report) {
        reportsService.addUserReport(report);
        return Result.success("举报提交成功，感谢您的反馈！");
    }
}
