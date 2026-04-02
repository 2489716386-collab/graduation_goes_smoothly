package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.FavoritesEntity;
import org.project.pet_health.entity.Users;
import org.project.pet_health.mapper.FavoritesMapper;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.service.FavoritesService;
import org.project.pet_health.service.InteractionNotificationsService;
import org.project.pet_health.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, FavoritesEntity> implements FavoritesService {

    @Autowired
    private CommunityPostsService postsService;

    @Autowired
    private InteractionNotificationsService noticeService;

    @Autowired
    private UsersService usersService;

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
    public IPage<CommunityPosts> getMyFavorites(Integer pageNum, Integer pageSize, Long userId) {
        // 1. 查收藏记录
        List<FavoritesEntity> favRecords = this.list(new LambdaQueryWrapper<FavoritesEntity>()
                .eq(FavoritesEntity::getUserId, userId));

        if (favRecords.isEmpty()) return new Page<>(pageNum, pageSize);

        List<Long> postIds = favRecords.stream().map(FavoritesEntity::getPostId).collect(Collectors.toList());

        // 2. 查动态内容
        List<CommunityPosts> posts = postsService.listByIds(postIds);

        // 3. 💡 补全作者信息
        Set<Long> authorIds = posts.stream().map(CommunityPosts::getUserId).collect(Collectors.toSet());
        Map<Long, Users> userMap = usersService.listByIds(authorIds).stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));

        posts.forEach(post -> {
            Users author = userMap.get(post.getUserId());
            if (author != null) {
                post.setNickname(author.getNickname());
                post.setAvatar(author.getAvatarUrl());
            }
            post.setIsFavorited(true);
        });

        // 4. 💡 按照帖子发布时间排序
        posts.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        // 5. 手动分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, posts.size());
        List<CommunityPosts> pageList = (start < posts.size()) ? posts.subList(start, end) : new ArrayList<>();

        IPage<CommunityPosts> page = new Page<>(pageNum, pageSize, posts.size());
        page.setRecords(pageList);
        return page;
    }
}
