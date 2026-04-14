package org.project.pet_health.controller;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.common.AdminAPI;
import org.project.pet_health.common.Result;
import org.project.pet_health.common.annotation.LogAction;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.Users;
import org.project.pet_health.enums.AuditStatus;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.service.UsersService;
import org.project.pet_health.utils.JwtUtil;
import org.project.pet_health.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community-posts")
@Tag(name = "社区动态审核管理")
public class CommunityPostsController {

    @Autowired
    private CommunityPostsService postsService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;


    @GetMapping("/admin/page")
    @Operation(summary = "【PC】后台分页条件查询社区动态(不含图片URL)")
    @AdminAPI
    public Result adminPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "20") Integer pageSize,
                            @RequestParam(required = false) Integer postType,
                            @RequestParam(required = false) AuditStatus status,
                            @RequestParam(required = false) String content,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {

        // Controller 极简，直接调用 Service 获取组装好的分页数据
        return Result.success(postsService.getAdminPage(pageNum, pageSize, postType, status, content, startDate, endDate));
    }

    @PostMapping("/admin/audit")
    @Operation(summary = "【PC】批量审核动态(修改状态并联动举报表)")
    // 使用 SpEL 表达式动态记录将帖子改成了什么状态
    @LogAction("审核了社区动态，动态id:#{#postIds}，目标状态为: #{#status.desc}")
    @AdminAPI
    public Result batchAudit(@RequestParam AuditStatus status, @RequestBody List<Long> postIds) {

        // 核心跨表修改逻辑已下沉到 Service 层的 batchAuditPosts 方法中
        postsService.batchAuditPosts(postIds, status);
        return Result.success("批量审核动态成功");
    }

    @GetMapping("/user/feed")
    @Operation(summary = "【小程序】获取社区动态大厅")
    public Result feed(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postsService.getCommunityFeed(pageNum, pageSize));
    }

    @PostMapping("/user/add")
    @Operation(summary = "【小程序】发布新动态")
    public Result addPost(@RequestBody CommunityPosts post, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");

        // ================= DFA 脱敏拦截开始 =================
        if (post.getContent() != null) {
            String cleanContent = sensitiveWordFilter.replaceSensitiveWord(post.getContent());
            post.setContent(cleanContent);
        }
        // ================= DFA 脱敏拦截结束 =================

        postsService.addUserPost(post, userId);
        return Result.success("发布成功");
    }

    @DeleteMapping("/user/delete/{postId}")
    @Operation(summary = "【小程序】删除自己的动态")
    public Result deletePost(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        postsService.deleteUserPost(postId, userId);
        return Result.success("删除成功");
    }

    @GetMapping("/user/my")
    @Operation(summary = "【小程序】获取我的动态列表")
    public Result getMyPosts(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             HttpServletRequest request) {
        // 复用你现有的获取当前登录用户ID的逻辑
        Long userId = (Long) request.getAttribute("currentUserId");
        // 调用 Service 层获取分页数据并返回
        return Result.success(postsService.getMyPosts(pageNum, pageSize, userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "【小程序】获取单条动态详情")
    public Result getPostDetail(@PathVariable Long id) {
        // 直接调用 MyBatis-Plus 自带的 getById 方法查询数据库
        CommunityPosts post = postsService.getById(id);

        Users user = usersService.getById(post.getUserId());
        if (user != null) {
            post.setNickname(user.getNickname());
            post.setAvatar(user.getAvatarUrl()); // 对应你 Users 表里的字段
        }
        return Result.success(post);
    }

    /**
     * 1. 搜索动态 (记录历史 + 支持游客与登录用户)
     */
    @GetMapping("/search")
    public Result search(@RequestParam("keyword") String keyword,
                         @RequestParam(value = "sort", defaultValue = "hot") String sort,
                         @RequestHeader(value = "token", required = false) String token) {
        Long userId = null;

        // 尝试解析 Token
        if (token != null && !token.trim().isEmpty()) {
            try {
                Claims claims = JwtUtil.verifyJwt(token);
                if (claims != null && claims.get("userId") != null) {
                    userId = Long.valueOf(claims.get("userId").toString());
                }
            } catch (Exception e) {
                // Token 伪造或已过期
                // 这里【不抛出异常】，而是默默吞掉，视为 userId = null (游客身份)
            }
        }

        // 调用我们在 Service 中写好的搜索逻辑
        List<CommunityPosts> list = postsService.searchPosts(keyword, sort, userId);
        return Result.success(list);
    }

    /**
     * 2. 个性化推荐动态 (千人千面 + 游客冷启动榜单)
     */
    @GetMapping("/recommend")
    public Result recommend(@RequestHeader(value = "token", required = false) String token) {
        Long userId = null;

        // 尝试解析 Token
        if (token != null && !token.trim().isEmpty()) {
            try {
                Claims claims = JwtUtil.verifyJwt(token);
                if (claims != null && claims.get("userId") != null) {
                    userId = Long.valueOf(claims.get("userId").toString());
                }
            } catch (Exception e) {
                // 同样【不抛出异常】，让过期用户或游客也能看到纯热度动态
            }
        }

        // 调用我们在 Service 中写好的推荐逻辑
        List<CommunityPosts> list = postsService.getRecommendedPosts(userId);
        return Result.success(list);
    }
}
