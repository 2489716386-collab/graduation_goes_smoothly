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

        // 2. 取出所有状态为已发布的帖子
        List<CommunityPosts> allPosts = communityPostsMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CommunityPosts>()
                        .eq(CommunityPosts::getStatus, org.project.pet_health.enums.AuditStatus.APPROVED)
        );

        // 如果没有搜索词，直接按热度或时间返回前20条即可
        if (!org.springframework.util.StringUtils.hasText(keyword)) {
            return fallbackToBasicSort(allPosts, sort);
        }

        // 3. 【核心修复】将用户的搜索词转化为向量
        List<Double> keywordVector = null;
        try {
            keywordVector = aiService.getEmbedding(keyword);
        } catch (Exception e) {
            log.error("获取搜索词向量失败", e);
        }

        // 4. 计算所有帖子与搜索词的语义相似度
        for (CommunityPosts post : allPosts) {
            if (post.getContentVector() != null && keywordVector != null && !keywordVector.isEmpty()) {
                List<Double> postVector = com.alibaba.fastjson2.JSON.parseArray(post.getContentVector(), Double.class);
                post.setSimilarityScore(calculateCosineSimilarity(keywordVector, postVector));
            } else {
                // 降级处理：如果没有向量数据，但文本包含关键词，给一个基础及格分保底
                if (post.getContent() != null && post.getContent().contains(keyword)) {
                    post.setSimilarityScore(0.5);
                } else {
                    post.setSimilarityScore(0.0);
                }
            }
        }

        // 5. 根据相似度过滤并排序
        List<CommunityPosts> resultPosts = allPosts.stream()
                // 过滤掉完全不相关的帖子（相似度低于0.15视作不相关，可根据实际效果微调）
                .filter(p -> p.getSimilarityScore() > 0.15)
                .sorted((p1, p2) -> {
                    if ("time".equals(sort)) {
                        // 按时间倒序
                        return p2.getCreateTime().compareTo(p1.getCreateTime());
                    } else {
                        // 【改进后的权重公式】：语义相似度(0~1)放大1000倍作为基础分，点赞数取对数后乘以10作为加权分
                        // 这样点赞数再高也不会超过相似度的决定性作用
                        double score1 = p1.getSimilarityScore() * 1000 + Math.log10(p1.getLikeCount() + 1) * 10;
                        double score2 = p2.getSimilarityScore() * 1000 + Math.log10(p2.getLikeCount() + 1) * 10;
                        return Double.compare(score2, score1); // 降序
                    }
                })
                .limit(20) // 取前20条
                .collect(Collectors.toList());

        // 6. 手动关联查询用户信息
        fillUserInfo(resultPosts);

        return resultPosts;
    }

    // 辅助方法：当没有搜索词时，退化为基础的热度/时间排序
    private List<CommunityPosts> fallbackToBasicSort(List<CommunityPosts> posts, String sort) {
        List<CommunityPosts> sortedPosts = posts.stream().sorted((p1, p2) -> {
            if ("time".equals(sort)) {
                return p2.getCreateTime().compareTo(p1.getCreateTime());
            } else {
                return Integer.compare(p2.getLikeCount() * 2 + p2.getCommentCount() * 5, p1.getLikeCount() * 2 + p1.getCommentCount() * 5);
            }
        }).limit(20).collect(Collectors.toList());
        fillUserInfo(sortedPosts);
        return sortedPosts;
    }

    // 辅助方法：填充用户信息
    private void fillUserInfo(List<CommunityPosts> posts) {
        if (posts != null && !posts.isEmpty()) {
            for (CommunityPosts post : posts) {
                org.project.pet_health.entity.Users user = usersMapper.selectById(post.getUserId());
                if (user != null) {
                    post.setNickname(user.getNickname());
                    post.setAvatar(user.getAvatarUrl());
                }
            }
        }
    }


    @Override
    public List<CommunityPosts> getRecommendedPosts(Long userId) {
        // 1. 构建“用户兴趣描述文本”
        StringBuilder userInterestText = new StringBuilder();

        if (userId != null) {
            // 获取用户的宠物品种偏好
            List<Pets> myPets = petsMapper.selectList(new LambdaQueryWrapper<Pets>().eq(Pets::getUserId, userId));
            myPets.forEach(p -> {
                PetBreeds breed = petBreedsMapper.selectById(p.getBreedId());
                if(breed != null) {
                    userInterestText.append(breed.getBreedName()).append(" ");
                }
            });

            // 获取最近的搜索词
            List<SearchHistoryEntity> histories = searchHistoryMapper.selectList(
                    new LambdaQueryWrapper<SearchHistoryEntity>().eq(SearchHistoryEntity::getUserId, userId).last("LIMIT 5")
            );
            histories.forEach(h -> userInterestText.append(h.getKeyword()).append(" "));
        }

        // 2. 将用户画像转化为“用户向量”
        List<Double> userVector = null;
        if (userInterestText.length() > 0) {
            try {
                userVector = aiService.getEmbedding(userInterestText.toString());
            } catch (Exception e) {
                log.error("AI 向量化用户兴趣失败", e);
            }
        }

        // 3. 获取所有待推荐帖子
        List<CommunityPosts> allPosts = this.list(new LambdaQueryWrapper<CommunityPosts>().eq(CommunityPosts::getStatus, AuditStatus.APPROVED));

        // 如果用户是完全的“白板”（没宠物也没搜索历史），退化为纯热度大盘推荐
        if (userVector == null || userVector.isEmpty()) {
            List<CommunityPosts> hotPosts = allPosts.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getLikeCount() * 2 + p2.getCommentCount() * 5, p1.getLikeCount() * 2 + p1.getCommentCount() * 5))
                    .limit(20)
                    .collect(Collectors.toList());
            fillUserInfo(hotPosts);
            return hotPosts;
        }

        // 4. 核心：计算每篇帖子与用户的余弦相似度
        for (CommunityPosts post : allPosts) {
            if (post.getContentVector() != null) {
                List<Double> postVector = com.alibaba.fastjson2.JSON.parseArray(post.getContentVector(), Double.class);
                post.setSimilarityScore(calculateCosineSimilarity(userVector, postVector));
            } else {
                post.setSimilarityScore(0.0);
            }
        }

        // 5. 【修改核心权重】：相似度占绝对主导，点赞数做对数降维后作为辅助加权分
        List<CommunityPosts> recommendedPosts = allPosts.stream()
                .sorted((p1, p2) -> {
                    // 相似度(0~1) * 1000 + log10(点赞数+1) * 20
                    // 加1是为了避免 log10(0) 报错负无穷
                    double score1 = p1.getSimilarityScore() * 1000 + Math.log10(p1.getLikeCount() + 1) * 20;
                    double score2 = p2.getSimilarityScore() * 1000 + Math.log10(p2.getLikeCount() + 1) * 20;
                    return Double.compare(score2, score1); // 降序
                })
                .limit(20)
                .collect(Collectors.toList());

        // 填充用户信息
        fillUserInfo(recommendedPosts);

        return recommendedPosts;
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
