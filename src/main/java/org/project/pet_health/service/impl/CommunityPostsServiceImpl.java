package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.project.pet_health.entity.*;
import org.project.pet_health.enums.AuditStatus;
import org.project.pet_health.enums.ReportStatus;
import org.project.pet_health.enums.TargetType;
import org.project.pet_health.mapper.*;
import org.project.pet_health.service.CommunityPostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<CommunityPosts> getRecommendedPosts(Long userId) {
        boolean isColdStart = true;
        List<String> petBreeds = new ArrayList<>();
        List<KeywordWeight> keywordWeights = new ArrayList<>();

        if (userId != null) {
            // 1. 获取用户宠物特征（基础权重很高，比如设为 10）
            List<Pets> myPets = petsMapper.selectList(
                    new LambdaQueryWrapper<Pets>().eq(Pets::getUserId, userId)
            );
            if (!myPets.isEmpty()) {
                isColdStart = false;
                // 使用 Stream API 提取所有非空的 breedId 并去重
                List<Integer> breedIds = myPets.stream()
                        .map(Pets::getBreedId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                if (!breedIds.isEmpty()) {
                    // MyBatis-Plus 的 selectBatchIds 接受 Collection<? extends Serializable>，传 Integer 列表完全没问题
                    List<PetBreeds> breedsList = petBreedsMapper.selectBatchIds(breedIds);

                    // 将查到的 breedName 收集到列表中
                    petBreeds = breedsList.stream()
                            .map(PetBreeds::getBreedName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
            }

            // 2. 获取最近 7 天的搜索历史，并进行【权重衰减计算】
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<SearchHistoryEntity> histories = searchHistoryMapper.selectList(
                    new LambdaQueryWrapper<SearchHistoryEntity>()
                            .eq(SearchHistoryEntity::getUserId, userId)
                            .ge(SearchHistoryEntity::getCreateTime, sevenDaysAgo)
                            .orderByDesc(SearchHistoryEntity::getCreateTime)
            );

            if (!histories.isEmpty()) {
                isColdStart = false;
                // 去重，保留同一个词最新的搜索时间计算权重
                Map<String, Integer> weightMap = new HashMap<>();
                for (SearchHistoryEntity history : histories) {
                    if (!weightMap.containsKey(history.getKeyword())) {
                        // 衰减公式：权重 = 5 - 相差天数 (今天搜的得5分，1天前得4分，以此类推，最低1分)
                        long daysBetween = ChronoUnit.DAYS.between(history.getCreateTime(), LocalDateTime.now());
                        int weight = Math.max(1, 5 - (int) daysBetween);
                        weightMap.put(history.getKeyword(), weight);
                    }
                }
                weightMap.forEach((k, w) -> keywordWeights.add(new KeywordWeight(k, w)));
            }
        }

        // 3. 调用 Mapper 执行动态 SQL 查询
        // 参数：是否冷启动、宠物特征列表、带权重的历史搜索词列表
        return communityPostsMapper.selectRecommendedPosts(isColdStart, petBreeds, keywordWeights);
    }

    /**
     * 搜索动态并记录搜索历史
     */
    public List<CommunityPosts> searchPosts(String keyword, String sort, Long userId) {
        // 1. 如果用户已登录且搜索词不为空，将关键词写入 search_history 表
        if (userId != null && keyword != null && !keyword.trim().isEmpty()) {
            SearchHistoryEntity history = new SearchHistoryEntity();
            history.setUserId(userId);
            history.setKeyword(keyword.trim());
            history.setCreateTime(LocalDateTime.now());
            searchHistoryMapper.insert(history);
        }

        // 2. 构建模糊查询条件 (内容包含关键词，或者标签包含关键词)
        QueryWrapper<CommunityPosts> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("content", keyword).or().like("tags", keyword));
        }

        // 3. 根据前端传来的 sort 参数进行排序
        if ("time".equals(sort)) {
            // 时间优先：最新发布的在前面
            wrapper.orderByDesc("create_time");
        } else {
            // 热度优先：使用 MyBatis-Plus 的 last() 直接追加底层 SQL 进行计算排序
            wrapper.last("ORDER BY (like_count * 2 + comment_count * 5) DESC");
        }

        return communityPostsMapper.selectList(wrapper);
    }
}
