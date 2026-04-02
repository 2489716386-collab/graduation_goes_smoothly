package org.project.pet_health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.dto.CommentDTO;
import org.project.pet_health.entity.Comments;
import org.project.pet_health.enums.AuditStatus;
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
                            @RequestParam(defaultValue = "20") Integer pageSize,
                            @RequestParam(required = false) AuditStatus status,
                            @RequestParam(required = false) String content,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {

        return Result.success(commentsService.getAdminPage(pageNum, pageSize, status, content, startDate, endDate));
    }

    @PostMapping("/admin/audit")
    @Operation(summary = "批量审核评论(修改状态并联动举报表)")
    @LogAction("审核了动态评论,评论id：#{#commentIds}，目标状态为: #{#status.desc}")
    public Result batchAudit(@RequestParam AuditStatus status, @RequestBody List<Long> commentIds) {

        commentsService.batchAuditComments(commentIds, status);
        return Result.success("批量审核评论成功");
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "【小程序】获取动态的评论列表")
    public Result getPostComments(@PathVariable Long postId) {
        return Result.success(commentsService.getCommentsByPostId(postId));
    }

    @PostMapping("/user/add")
    @Operation(summary = "【小程序】发表评论")
    public Result addComment(@RequestBody Comments comment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        commentsService.addComment(comment, userId);
        return Result.success("评论成功");
    }

    @GetMapping("/tree/{postId}")
    @Operation(summary = "获取帖子的树形评论列表")
    public Result<List<CommentDTO>> getTree(@PathVariable Long postId) {
        return Result.success(commentsService.getTreeComments(postId));
    }

    @PostMapping("/user/add")
    @Operation(summary = "发表评论/回复")
    public Result add(@RequestBody Comments comment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        commentsService.postComment(comment, userId);
        return Result.success("发表成功");
    }
}
