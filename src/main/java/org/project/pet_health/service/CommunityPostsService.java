package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.enums.AuditStatus;

import java.util.List;

public interface CommunityPostsService extends IService<CommunityPosts> {
    // 后台分页条件查询动态 (不显示 media_urls)
    Page<CommunityPosts> getAdminPage(Integer pageNum, Integer pageSize, Integer postType, AuditStatus status, String content, String startDate, String endDate);

    // 批量审核动态 (联动更新举报记录状态)
    void batchAuditPosts(List<Long> postIds, AuditStatus status);

    //用户端操作
    void addUserPost(CommunityPosts post, Long userId);
    void deleteUserPost(Long postId, Long userId);
    Page<CommunityPosts> getCommunityFeed(Integer pageNum, Integer pageSize);

    Page<CommunityPosts> getMyPosts(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 搜索动态并记录搜索历史
     */
    List<CommunityPosts> getRecommendedPosts(Long userId);

    /**
     * 获取个性化推荐动态
     */
    List<CommunityPosts> searchPosts(String keyword, String sort, Long userId);

    boolean save(CommunityPosts entity);
}
