package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.AdminAPI;
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
    @AdminAPI
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "20") Integer pageSize,
                            @RequestParam(required = false) String targetType,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(required = false) String reason,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate,
                            @RequestParam(required = false) Long targetId) {
        IPage<Reports> page = reportsService.pageAdmin(pageNum, pageSize, targetType, status, startDate, endDate, reason, targetId);
        return Result.success(page);
    }

    @PostMapping("/user/add")
    @Operation(summary = "【用户端】新增举报记录")
    // 这里是用户操作，不加 @LogAction 管理员日志
    public Result addUserReport(@RequestBody Reports report) {
        reportsService.addUserReport(report);
        return Result.success("举报提交成功，感谢您的反馈！");
    }
}
