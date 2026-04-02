package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.LikesEntity;
import org.project.pet_health.entity.Users;
import org.project.pet_health.mapper.LikesMapper;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.service.InteractionNotificationsService;
import org.project.pet_health.service.LikesService;
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
public class LikesServiceImpl extends ServiceImpl<LikesMapper, LikesEntity> implements LikesService {

    @Autowired
    private CommunityPostsService postsService;

    @Autowired
    private InteractionNotificationsService noticeService;

    @Autowired
    private UsersService usersService;

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
    public IPage<CommunityPosts> getMyLikesPosts(Integer pageNum, Integer pageSize, Long userId) {
        // 1. 先查出该用户所有的点赞记录
        List<LikesEntity> likeRecords = this.list(new LambdaQueryWrapper<LikesEntity>()
                .eq(LikesEntity::getUserId, userId));

        // 【边界防御】如果没有点赞记录，直接返回空分页
        if (likeRecords.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        // 2. 提取出所有的帖子 ID
        List<Long> postIds = likeRecords.stream()
                .map(LikesEntity::getPostId)
                .collect(Collectors.toList());

        // 3. 批量查询这些动态的详细内容
        List<CommunityPosts> posts = postsService.listByIds(postIds);

        // 【边界防御】如果帖子都被原作者删除了，导致查不到内容，直接返回空分页
        if (posts.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        // 4. 💡 核心逻辑：提取发帖人 ID 并批量查询用户信息，避免 for 循环里查数据库（N+1 查询问题）
        Set<Long> authorIds = posts.stream()
                .map(CommunityPosts::getUserId)
                .collect(Collectors.toSet());

        Map<Long, Users> userMap = usersService.listByIds(authorIds).stream()
                .collect(Collectors.toMap(Users::getUserId, u -> u));

        // 5. 将头像、昵称和点赞状态组装到帖子实体中
        posts.forEach(post -> {
            Users author = userMap.get(post.getUserId());
            if (author != null) {
                post.setNickname(author.getNickname());
                post.setAvatar(author.getAvatarUrl());
            }
            // 既然是在“我的喜欢”列表里，那状态必须是 true
            post.setIsLiked(true);
        });

        // 6. 💡 核心逻辑：按照动态的实际发布时间降序排序（从新到旧）
        posts.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        // 7. 手动对排序好的 List 进行内存分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, posts.size());

        List<CommunityPosts> pageList = new ArrayList<>();
        if (start < posts.size()) {
            pageList = posts.subList(start, end);
        }

        // 8. 组装标准的 IPage 对象返回给 Controller
        IPage<CommunityPosts> page = new Page<>(pageNum, pageSize, posts.size());
        page.setRecords(pageList);

        return page;
    }
}
