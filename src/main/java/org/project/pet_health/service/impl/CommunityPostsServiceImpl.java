package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.Reports;
import org.project.pet_health.entity.SearchHistoryEntity;
import org.project.pet_health.enums.AuditStatus;
import org.project.pet_health.enums.ReportStatus;
import org.project.pet_health.enums.TargetType;
import org.project.pet_health.mapper.*;
import org.project.pet_health.service.CommunityPostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommunityPostsServiceImpl extends ServiceImpl<CommunityPostsMapper, CommunityPosts> implements CommunityPostsService {

    @Autowired
    private ReportsMapper reportsMapper;

    @Resource
    private CommunityPostsMapper communityPostsMapper;
    @Resource
    private PetsMapper petsMapper;
    @Resource
    private PetBreedsMapper petBreedsMapper; // 新增：注入品种表的 Mapper
    @Resource
    private SearchHistoryMapper searchHistoryMapper;
    @Resource
    private UsersMapper usersMapper;

    @Override
    public Page<CommunityPosts> getAdminPage(Integer pageNum, Integer pageSize, Integer postType, AuditStatus status, String content, String startDate, String endDate) {
        LambdaQueryWrapper<CommunityPosts> wrapper = new LambdaQueryWrapper<>();

        // 【核心】过滤掉 media_urls 字段不查询，减少宽带消耗
        wrapper.select(CommunityPosts.class, info -> !info.getColumn().equals("media_urls"));

        if (postType != null) wrapper.eq(CommunityPosts::getPostType, postType);
        if (status != null) wrapper.eq(CommunityPosts::getStatus, status);
        if (StringUtils.hasText(content)) wrapper.like(CommunityPosts::getContent, content);
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            wrapper.between(CommunityPosts::getCreateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        }
        wrapper.orderByDesc(CommunityPosts::getPostId);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAuditPosts(List<Long> postIds, AuditStatus status) {
        if (postIds == null || postIds.isEmpty()) return;

        // 1. 批量修改动态表状态 (例如：1=已发布, 2=违规拦截)
        this.update(new LambdaUpdateWrapper<CommunityPosts>()
                .in(CommunityPosts::getPostId, postIds)
                .set(CommunityPosts::getStatus, status));

        // 2. 【状态联动】同步修改举报记录表中的处理状态
        ReportStatus reportStatus = null;
        if (AuditStatus.REJECTED.equals(status)) {
            reportStatus = ReportStatus.DELETED;; // 如果动态被拦截，举报状态变为：2-已删除内容
        } else if (AuditStatus.APPROVED.equals(status)) {
            reportStatus = ReportStatus.IGNORED; // 如果动态恢复发布，举报状态变为：1-已忽略
        }

        if (reportStatus != null) {
            reportsMapper.update(null, new LambdaUpdateWrapper<Reports>()
                    .eq(Reports::getTargetType, TargetType.POST)
                    .in(Reports::getTargetId, postIds)
                    .set(Reports::getStatus, reportStatus));
        }
    }

    @Override
    public void addUserPost(CommunityPosts post, Long userId) {
        post.setUserId(userId);
        // 使用我们之前定义好的枚举：默认状态为已发布
        // TODO: 如果你接了敏感词过滤接口，可以在这里判断，有敏感词就置为 PENDING
        post.setStatus(AuditStatus.APPROVED);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setReportCount(0);
        this.save(post);
    }

    @Override
    public void deleteUserPost(Long postId, Long userId) {
        // 只能删除自己的帖子
        this.remove(new LambdaQueryWrapper<CommunityPosts>()
                .eq(CommunityPosts::getPostId, postId)
                .eq(CommunityPosts::getUserId, userId));
    }

    @Override
    public Page<CommunityPosts> getCommunityFeed(Integer pageNum, Integer pageSize) {
        // 社区主页瀑布流：只查状态为 APPROVED (已发布) 的帖子，按时间倒序
        return this.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<CommunityPosts>()
                        .eq(CommunityPosts::getStatus, AuditStatus.APPROVED)
                        .orderByDesc(CommunityPosts::getPostId));
    }

    @Override
    public Page<CommunityPosts> getMyPosts(Integer pageNum, Integer pageSize, Long userId) {
        LambdaQueryWrapper<CommunityPosts> wrapper = new LambdaQueryWrapper<>();
        // 查询当前用户的动态，并按创建时间（或主键ID）倒序排列
        wrapper.eq(CommunityPosts::getUserId, userId)
                .orderByDesc(CommunityPosts::getPostId);
        // 如果你的实体类里有 createTime，也可以用 .orderByDesc(CommunityPosts::getCreateTime)

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }


    // 内部类：用于封装带权重的搜索词传给 MyBatis
    public static class KeywordWeight {
        public String keyword;
        public Integer weight;
        public KeywordWeight(String k, Integer w) { this.keyword = k; this.weight = w; }
    }

    /**
     * 1. 搜索动态 (记录历史 + 排序 + 关联用户信息)
     */
    @Override
    public List<CommunityPosts> searchPosts(String keyword, String sort, Long userId) {
        // 1. 如果用户已登录且搜索词不为空，将关键词写入 search_history 表
        if (userId != null && org.springframework.util.StringUtils.hasText(keyword)) {
            SearchHistoryEntity history = new SearchHistoryEntity();
            history.setUserId(userId);
            history.setKeyword(keyword.trim());
            history.setCreateTime(java.time.LocalDateTime.now());
            searchHistoryMapper.insert(history);
        }

        // 2. 构造查询条件 (只查内容 content，且状态为 APPROVED)
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CommunityPosts> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(CommunityPosts::getStatus, org.project.pet_health.enums.AuditStatus.APPROVED);

        if (org.springframework.util.StringUtils.hasText(keyword)) {
            wrapper.like(CommunityPosts::getContent, keyword.trim());
        }

        // 3. 排序
        if ("time".equals(sort)) {
            wrapper.orderByDesc(CommunityPosts::getCreateTime);
        } else {
            wrapper.last("ORDER BY (like_count * 2 + comment_count * 5) DESC");
        }

        // 4. 执行查询
        List<CommunityPosts> posts = communityPostsMapper.selectList(wrapper);

        // 5. 【核心修复】手动关联查询用户信息（昵称和头像）
        if (!posts.isEmpty()) {
            for (CommunityPosts post : posts) {
                org.project.pet_health.entity.Users user = usersMapper.selectById(post.getUserId());
                if (user != null) {
                    post.setNickname(user.getNickname());
                    post.setAvatar(user.getAvatarUrl());
                }
            }
        }

        return posts;
    }


    /**
     * 2. 获取个性化推荐动态
     */
    @Override
    public List<CommunityPosts> getRecommendedPosts(Long userId) {
        boolean isColdStart = true;
        List<String> petBreeds = new ArrayList<>();
        List<KeywordWeight> keywordWeights = new ArrayList<>();

        if (userId != null) {
            // 获取宠物特征
            List<org.project.pet_health.entity.Pets> myPets = petsMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.project.pet_health.entity.Pets>()
                            .eq(org.project.pet_health.entity.Pets::getUserId, userId)
            );

            if (!myPets.isEmpty()) {
                isColdStart = false;
                List<Integer> breedIds = myPets.stream()
                        .map(org.project.pet_health.entity.Pets::getBreedId)
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .collect(java.util.stream.Collectors.toList());

                if (!breedIds.isEmpty()) {
                    List<org.project.pet_health.entity.PetBreeds> breedsList = petBreedsMapper.selectBatchIds(breedIds);
                    petBreeds = breedsList.stream()
                            .map(org.project.pet_health.entity.PetBreeds::getBreedName)
                            .filter(java.util.Objects::nonNull)
                            .collect(java.util.stream.Collectors.toList());
                }
            }

            // 获取搜索历史（时间衰减）
            java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7);
            List<SearchHistoryEntity> histories = searchHistoryMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchHistoryEntity>()
                            .eq(SearchHistoryEntity::getUserId, userId)
                            .ge(SearchHistoryEntity::getCreateTime, sevenDaysAgo)
                            .orderByDesc(SearchHistoryEntity::getCreateTime)
            );

            if (!histories.isEmpty()) {
                isColdStart = false;
                java.util.Map<String, Integer> weightMap = new java.util.HashMap<>();
                for (SearchHistoryEntity history : histories) {
                    if (!weightMap.containsKey(history.getKeyword())) {
                        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(history.getCreateTime(), java.time.LocalDateTime.now());
                        int weight = Math.max(1, 5 - (int) daysBetween);
                        weightMap.put(history.getKeyword(), weight);
                    }
                }
                weightMap.forEach((k, w) -> keywordWeights.add(new KeywordWeight(k, w)));
            }
        }

        // 调用 Mapper 执行动态 SQL 查询 (注意最后一个参数传入了枚举)
        return communityPostsMapper.selectRecommendedPosts(
                isColdStart,
                petBreeds,
                keywordWeights,
                org.project.pet_health.enums.AuditStatus.APPROVED // 👈 新增的枚举参数
        );
    }
}
