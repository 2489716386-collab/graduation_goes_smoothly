package org.project.pet_health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.project.pet_health.entity.Users;

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
public interface UsersMapper extends BaseMapper<Users> {

    //查询最近7天的新增用户统计
    @Select("SELECT DATE_FORMAT(create_time, '%m-%d') as date, COUNT(*) as count " +
            "FROM users " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY date " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getUserTrend();

    //添加查询本月新增用户的接口
    @Select("SELECT COUNT(*) FROM users WHERE DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')")
    Integer getMonthlyUsersCount();
 }
