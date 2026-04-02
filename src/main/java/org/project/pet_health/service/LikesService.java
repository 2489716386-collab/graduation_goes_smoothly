package org.project.pet_health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.LikesEntity;

public interface LikesService extends IService<LikesEntity> {
    /**
     * 点赞/取消点赞 切换逻辑
     * @return true 代表已点赞，false 代表取消点赞
     */
    boolean toggleLike(Long postId, Long userId);

    /**
     * 分页获取用户点赞的帖子列表
     */
    IPage<CommunityPosts> getMyLikesPosts(Integer pageNum, Integer pageSize, Long userId);
}
