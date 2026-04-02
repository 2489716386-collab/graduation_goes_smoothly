package org.project.pet_health.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.FavoritesEntity;

public interface FavoritesService extends IService<FavoritesEntity> {

    /**
     * 切换收藏状态
     * @return true-已收藏, false-已取消收藏
     */
    boolean toggleFavorite(Long postId, Long userId);

    /**
     * 获取我收藏的动态列表
     */
    IPage<CommunityPosts> getMyFavorites(Integer pageNum, Integer pageSize, Long userId);
}
