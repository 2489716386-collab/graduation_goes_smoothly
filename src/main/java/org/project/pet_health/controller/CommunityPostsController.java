package org.project.pet_health.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.service.CommunityPostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/posts")
@Tag(name = "PC后台-社区内容审核")
public class CommunityPostsController {

    @Autowired
    private CommunityPostsService postsService;

    @GetMapping("/page")
    @Operation(summary = "分页查询社区帖子(支持按状态筛选)")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer status) { // 状态：0审核中, 1已发布, 2拦截
        Page<CommunityPosts> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommunityPosts> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(CommunityPosts::getStatus, status);
        }
        wrapper.orderByDesc(CommunityPosts::getCreateTime);

        return Result.success(postsService.page(page, wrapper));
    }

    @PutMapping("/audit")
    @Operation(summary = "审核帖子(修改状态)")
    public Result audit(@RequestBody CommunityPosts post) {
        // 前端只需传入 postId 和要修改成的 status
        postsService.updateById(post);
        return Result.success("审核操作成功");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "物理删除违规帖子")
    public Result delete(@PathVariable Long id) {
        postsService.removeById(id);
        return Result.success("删除成功");
    }
}
