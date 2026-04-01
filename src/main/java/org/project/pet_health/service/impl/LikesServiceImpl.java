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
        // 1. 检查是否已经点赞
        LambdaQueryWrapper<LikesEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikesEntity::getPostId, postId).eq(LikesEntity::getUserId, userId);
        LikesEntity existing = this.getOne(wrapper);

        CommunityPosts post = postsService.getById(postId);
        if (post == null) throw new RuntimeException("帖子不存在");

        if (existing != null) {
            // 2. 如果已存在 -> 取消点赞（物理删除）
            this.removeById(existing.getLikeId());
            // 同步减少帖子的点赞数
            postsService.update().setSql("likes_count = likes_count - 1")
                    .eq("post_id", postId).gt("likes_count", 0).update();
            return false;
        } else {
            // 3. 如果不存在 -> 新增点赞
            LikesEntity newLike = new LikesEntity();
            newLike.setPostId(postId);
            newLike.setUserId(userId);
            this.save(newLike);
            // 同步增加帖子的点赞数
            postsService.update().setSql("likes_count = likes_count + 1")
                    .eq("post_id", postId).update();

            // 4. 触发互动通知
            noticeService.sendNotice(post.getUserId(), userId, "LIKE", postId, null);
            return true;
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
