package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.LikesEntity;
import org.project.pet_health.mapper.LikesMapper;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.service.InteractionNotificationsService;
import org.project.pet_health.service.LikesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikesServiceImpl extends ServiceImpl<LikesMapper, LikesEntity> implements LikesService {

    @Autowired
    private CommunityPostsService postsService;

    @Autowired
    private InteractionNotificationsService noticeService;

    @Override
    @Transactional // 开启事务，保证点赞记录和计数的同步
    public boolean toggleLike(Long postId, Long userId) {
        // 1. 检查是否已经点赞过
        LambdaQueryWrapper<LikesEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikesEntity::getPostId, postId).eq(LikesEntity::getUserId, userId);
        LikesEntity existing = this.getOne(wrapper);

        // 2. 获取帖子对象
        CommunityPosts post = postsService.getById(postId);
        if (post == null) throw new RuntimeException("帖子不存在");

        // 获取当前点赞数（如果是 null 就当做 0 处理，防止空指针）
        int currentLikes = post.getLikeCount() == null ? 0 : post.getLikeCount();

        if (existing != null) {
            // ================= 3. 已存在 -> 取消点赞 =================
            // 删除点赞记录
            this.removeById(existing.getLikeId());

            // 💡 稳健更新：点赞数 -1 (用 Math.max 确保不会减成负数)
            post.setLikeCount(Math.max(0, currentLikes - 1));
            postsService.updateById(post);

            return false; // 返回 false 代表当前状态为未赞

        } else {
            // ================= 4. 不存在 -> 新增点赞 =================
            // 插入点赞记录
            LikesEntity newLike = new LikesEntity();
            newLike.setPostId(postId);
            newLike.setUserId(userId);
            this.save(newLike);

            // 💡 稳健更新：点赞数 +1
            post.setLikeCount(currentLikes + 1);
            postsService.updateById(post);

            // 触发互动通知（通知帖子作者）
            noticeService.sendNotice(post.getUserId(), userId, "LIKE", postId, null);

            return true; // 返回 true 代表当前状态为已赞
        }
    }

    @Override
    public Page<CommunityPosts> getMyLikedPosts(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 先查出该用户所有的点赞记录
        LambdaQueryWrapper<LikesEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikesEntity::getUserId, userId).orderByDesc(LikesEntity::getCreateTime);
        List<LikesEntity> likes = this.list(wrapper);

        if (likes.isEmpty()) return new Page<>();

        // 2. 提取帖子ID并分页查询帖子详情
        List<Long> postIds = likes.stream().map(LikesEntity::getPostId).collect(Collectors.toList());
        Page<CommunityPosts> page = new Page<>(pageNum, pageSize);
        return postsService.page(page, new LambdaQueryWrapper<CommunityPosts>()
                .in(CommunityPosts::getPostId, postIds)
                .orderByDesc(CommunityPosts::getCreateTime));
    }
}
