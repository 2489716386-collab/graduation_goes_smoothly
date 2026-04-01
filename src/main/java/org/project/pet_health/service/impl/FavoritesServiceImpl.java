package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.FavoritesEntity;
import org.project.pet_health.mapper.FavoritesMapper;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.service.FavoritesService;
import org.project.pet_health.service.InteractionNotificationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, FavoritesEntity> implements FavoritesService {

    @Autowired
    private CommunityPostsService postsService;

    @Autowired
    private InteractionNotificationsService noticeService;

    @Override
    @Transactional
    public boolean toggleFavorite(Long postId, Long userId) {
        // 1. 检查是否已经收藏过
        LambdaQueryWrapper<FavoritesEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoritesEntity::getPostId, postId).eq(FavoritesEntity::getUserId, userId);
        FavoritesEntity existing = this.getOne(wrapper);

        if (existing != null) {
            // 2. 已存在 -> 取消收藏（物理删除）
            this.removeById(existing.getId());
            return false;
        } else {
            // 3. 不存在 -> 执行收藏
            FavoritesEntity fav = new FavoritesEntity();
            fav.setPostId(postId);
            fav.setUserId(userId);
            this.save(fav);

            // 4. 发送互动通知给帖子作者
            CommunityPosts post = postsService.getById(postId);
            if (post != null) {
                noticeService.sendNotice(post.getUserId(), userId, "FAVORITE", postId, null);
            }
            return true;
        }
    }

    @Override
    public Page<CommunityPosts> getMyFavorites(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 获取该用户的所有收藏记录（按时间倒序）
        LambdaQueryWrapper<FavoritesEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FavoritesEntity::getUserId, userId).orderByDesc(FavoritesEntity::getCreateTime);
        List<FavoritesEntity> favList = this.list(wrapper);

        if (favList.isEmpty()) {
            return new Page<>();
        }

        // 2. 提取收藏的动态ID列表
        List<Long> postIds = favList.stream()
                .map(FavoritesEntity::getPostId)
                .collect(Collectors.toList());

        // 3. 分页查询对应的动态详情
        Page<CommunityPosts> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommunityPosts> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.in(CommunityPosts::getPostId, postIds)
                .orderByDesc(CommunityPosts::getCreateTime); // 列表按动态发布时间倒序排

        return postsService.page(page, postWrapper);
    }
}
