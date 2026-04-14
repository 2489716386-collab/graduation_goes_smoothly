package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.AdminAPI;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.dto.CommentDTO;
import org.project.pet_health.entity.Comments;
import org.project.pet_health.enums.AuditStatus;
import org.project.pet_health.service.CommentsService;
import org.project.pet_health.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@Tag(name = "PC后台-动态评论审核管理")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @GetMapping("/admin/page")
    @Operation(summary = "后台分页条件查询评论")
    @AdminAPI
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "20") Integer pageSize,
                            @RequestParam(required = false) AuditStatus status,
                            @RequestParam(required = false) String content,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {
        return Result.success(commentsService.getAdminPage(pageNum, pageSize, status, content, startDate, endDate));
    }

    @PostMapping("/admin/audit")
    @Operation(summary = "批量审核评论(修改状态并联动举报表)")
    @AdminAPI
    @LogAction("审核了动态评论,评论id：#{#commentIds}，目标状态为: #{#status.desc}")
    public Result batchAudit(@RequestParam AuditStatus status, @RequestBody List<Long> commentIds) {
        commentsService.batchAuditComments(commentIds, status);
        return Result.success("批量审核评论成功");
    }

    // ================= 核心升级：树形评论列表与发布 =================

    @GetMapping("/tree/{postId}")
    @Operation(summary = "【小程序】获取帖子的树形评论列表")
    public Result<List<CommentDTO>> getTree(@PathVariable Long postId) {
         // 🆘 加上这行打印！
        System.out.println(">>> 收到评论查询请求，帖子ID: " + postId);

        List<CommentDTO> treeComments = commentsService.getTreeComments(postId);

        // 🆘 加上这行打印！
        System.out.println(">>> 查询结果数量: " + (treeComments == null ? 0 : treeComments.size()));

        return Result.success(treeComments);
    }

    @PostMapping("/user/add")
    @Operation(summary = "【小程序】发表评论/回复")
    public Result add(@RequestBody Comments comment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");

        // ================= DFA 脱敏拦截开始 =================
        if (comment.getContent() != null) {
            String cleanContent = sensitiveWordFilter.replaceSensitiveWord(comment.getContent());
            comment.setContent(cleanContent);
        }
        // ================= DFA 脱敏拦截结束 =================

        commentsService.postComment(comment, userId);
        return Result.success("发表成功");
    }
}
