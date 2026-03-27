package org.project.pet_health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.project.pet_health.entity.CommunityPosts;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author weiling
 * @since 2026-03-11
 */
@Mapper
public interface CommunityPostsMapper extends BaseMapper<CommunityPosts> {

    @Select("SELECT DATE_FORMAT(create_time, '%m-%d') as date, COUNT(*) as count " +
            "FROM community_posts " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY date " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getPostTrend();

    // 在添加查询本月新增动态的接口
    @Select("SELECT COUNT(*) FROM community_posts WHERE DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')")
    Integer getMonthlyPostsCount();
}
