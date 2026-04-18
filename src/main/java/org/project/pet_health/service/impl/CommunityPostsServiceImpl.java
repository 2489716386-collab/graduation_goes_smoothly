package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.project.pet_health.entity.*;
import org.project.pet_health.enums.AuditStatus;
import org.project.pet_health.enums.ReportStatus;
import org.project.pet_health.enums.TargetType;
import org.project.pet_health.mapper.*;
import org.project.pet_health.service.AiService;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
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
    @Resource
    private UsersMapper usersMapper;

    @Resource
    private AiService aiService; // 注入你刚改好的 AI 服务

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Override
    public Page<CommunityPosts> getAdminPage(Integer pageNum, Integer pageSize, Integer postType, AuditStatus status, String content, String startDate, String endDate) {
        LambdaQueryWrapper<CommunityPosts> wrapper = new LambdaQueryWrapper<>();

        // 【核心】过滤掉 media_urls 字段不查询，减少宽带消耗
        //wrapper.select(CommunityPosts.class, info -> !info.getColumn().equals("media_urls"));

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

        // 1. 经过 DFA 算法过滤敏感词，将敏感词替换为 *
        if (StringUtils.hasText(post.getContent())) {
            String filteredContent = sensitiveWordFilter.replaceSensitiveWord(post.getContent());
            post.setContent(filteredContent);
        }

        // 2. 无论是否替换过，状态直接标记为已发布
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

        // 核心条件：查询当前用户的动态
        wrapper.eq(CommunityPosts::getUserId, userId)
                // 1. 先按时间倒序排列
                .orderByDesc(CommunityPosts::getCreateTime)
                // 2. 关键防重复：加上按 ID 倒序排列（彻底解决分页下滑出现重复数据的 Bug）
                .orderByDesc(CommunityPosts::getPostId);

        Page<CommunityPosts> page = new Page<>(pageNum, pageSize);
        return this.page(page, wrapper);
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


    @Override
    public List<CommunityPosts> getRecommendedPosts(Long userId) {
        // 1. 构建“用户兴趣描述文本”
        StringBuilder userInterestText = new StringBuilder();

        if (userId != null) {
            // 获取用户的宠物品种偏好
            List<Pets> myPets = petsMapper.selectList(new LambdaQueryWrapper<Pets>().eq(Pets::getUserId, userId));
            myPets.forEach(p -> userInterestText.append(petBreedsMapper.selectById(p.getBreedId()).getBreedName()).append(" "));

            // 获取最近的搜索词
            List<SearchHistoryEntity> histories = searchHistoryMapper.selectList(
                    new LambdaQueryWrapper<SearchHistoryEntity>().eq(SearchHistoryEntity::getUserId, userId).last("LIMIT 5")
            );
            histories.forEach(h -> userInterestText.append(h.getKeyword()).append(" "));
        }

        // 2. 将用户画像转化为“用户向量”
        List<Double> userVector = aiService.getEmbedding(userInterestText.toString());

        // 3. 获取所有待推荐帖子
        List<CommunityPosts> allPosts = this.list(new LambdaQueryWrapper<CommunityPosts>().eq(CommunityPosts::getStatus, AuditStatus.APPROVED));

        // 4. 核心：计算每篇帖子与用户的余弦相似度
        for (CommunityPosts post : allPosts) {
            if (post.getContentVector() != null) {
                List<Double> postVector = com.alibaba.fastjson2.JSON.parseArray(post.getContentVector(), Double.class);
                post.setSimilarityScore(calculateCosineSimilarity(userVector, postVector));
            } else {
                post.setSimilarityScore(0.0);
            }
        }

        // 5. 综合排序：相似度占 80% 权重，点赞数占 20% 权重
        return allPosts.stream()
                .sorted((p1, p2) -> {
                    double score1 = p1.getSimilarityScore() * 100 + p1.getLikeCount();
                    double score2 = p2.getSimilarityScore() * 100 + p2.getLikeCount();
                    return Double.compare(score2, score1); // 降序
                })
                .limit(20)
                .peek(post -> {
                    // 别忘了填充你之前做的昵称和头像
                    Users user = usersMapper.selectById(post.getUserId());
                    if(user != null) {
                        post.setNickname(user.getNickname());
                        post.setAvatar(user.getAvatarUrl());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean save(CommunityPosts entity) {
        // 1. 获取文本的语义向量
        try {
            java.util.List<Double> vector = aiService.getEmbedding(entity.getContent());
            if (vector != null && !vector.isEmpty()) {
                // 使用 fastjson2 将数组转为 JSON 字符串存入
                entity.setContentVector(com.alibaba.fastjson2.JSON.toJSONString(vector));
            }
        } catch (Exception e) {
            log.error("AI 向量化失败，但不影响帖子发布", e);
        }
        // 2. 调用原有的保存逻辑
        return super.save(entity);
    }


    /**
     * 辅助方法：余弦相似度数学公式实现
     */
    private double calculateCosineSimilarity(List<Double> vecA, List<Double> vecB) {
        if (vecA == null || vecB == null || vecA.size() != vecB.size() || vecA.isEmpty()) return 0;
        double dotProduct = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vecA.size(); i++) {
            dotProduct += vecA.get(i) * vecB.get(i);
            normA += Math.pow(vecA.get(i), 2);
            normB += Math.pow(vecB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
