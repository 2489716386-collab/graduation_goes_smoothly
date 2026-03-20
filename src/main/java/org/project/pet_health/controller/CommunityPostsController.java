package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.service.CommunityPostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community-posts")
@Tag(name = "PC后台-社区动态审核管理")
public class CommunityPostsController {

    @Autowired
    private CommunityPostsService postsService;

    @GetMapping("/admin/page")
    @Operation(summary = "后台分页条件查询社区动态(不含图片URL)")
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) Integer postType,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(required = false) String content,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {

        // Controller 极简，直接调用 Service 获取组装好的分页数据
        return Result.success(postsService.getAdminPage(pageNum, pageSize, postType, status, content, startDate, endDate));
    }

    @PostMapping("/admin/audit")
    @Operation(summary = "批量审核动态(修改状态并联动举报表)")
    // 使用 SpEL 表达式动态记录将帖子改成了什么状态
    @LogAction("批量审核了社区动态，目标状态为: #{#status}")
    public Result batchAudit(@RequestParam Integer status, @RequestBody List<Long> postIds) {

        // 核心跨表修改逻辑已下沉到 Service 层的 batchAuditPosts 方法中
        postsService.batchAuditPosts(postIds, status);
        return Result.success("批量审核动态成功");
    }
}
