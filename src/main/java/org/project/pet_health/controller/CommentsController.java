package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@Tag(name = "PC后台-动态评论审核管理")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    @GetMapping("/admin/page")
    @Operation(summary = "后台分页条件查询评论")
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(required = false) String content,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {

        return Result.success(commentsService.getAdminPage(pageNum, pageSize, status, content, startDate, endDate));
    }

    @PostMapping("/admin/audit")
    @Operation(summary = "批量审核评论(修改状态并联动举报表)")
    @LogAction("批量审核了动态评论，目标状态为: #{#status}")
    public Result batchAudit(@RequestParam Integer status, @RequestBody List<Long> commentIds) {

        commentsService.batchAuditComments(commentIds, status);
        return Result.success("批量审核评论成功");
    }
}
