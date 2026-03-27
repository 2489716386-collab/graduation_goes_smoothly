package org.project.pet_health.service.impl;

import jakarta.annotation.Resource;
import org.project.pet_health.dto.DashboardStatsDTO;
import org.project.pet_health.dto.TrendDTO;
import org.project.pet_health.mapper.CommunityPostsMapper;
import org.project.pet_health.mapper.UsersMapper;
import org.project.pet_health.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private UsersMapper usersMapper;

    @Resource
    private CommunityPostsMapper communityPostsMapper;

    @Override
    public TrendDTO getTrendData() {
        TrendDTO vo = new TrendDTO();
        List<String> dates = new ArrayList<>();
        List<Integer> userCounts = new ArrayList<>();
        List<Integer> postCounts = new ArrayList<>();

        // 1. 生成近7天的日期字符串 (X轴格式: MM-dd)
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            dates.add(today.minusDays(i).format(formatter));
        }

        // 2. 从数据库查询分组统计结果
        List<Map<String, Object>> userList = usersMapper.getUserTrend();
        List<Map<String, Object>> postList = communityPostsMapper.getPostTrend();

        // 3. 将数据库结果转换为 Map 方便快速按日期提取 (date -> count)
        Map<String, Integer> userMap = userList.stream().collect(
                Collectors.toMap(m -> m.get("date").toString(), m -> ((Number) m.get("count")).intValue()));
        Map<String, Integer> postMap = postList.stream().collect(
                Collectors.toMap(m -> m.get("date").toString(), m -> ((Number) m.get("count")).intValue()));

        // 4. 遍历近7天日期，匹配数据库数据，没有数据的天数自动填 0
        for (String date : dates) {
            userCounts.add(userMap.getOrDefault(date, 0));
            postCounts.add(postMap.getOrDefault(date, 0));
        }

        vo.setDates(dates);
        vo.setUserCounts(userCounts);
        vo.setPostCounts(postCounts);

        return vo;
    }

    @Override
    public DashboardStatsDTO getStatsData() {
        DashboardStatsDTO dto = new DashboardStatsDTO(); // 改为实例化 DTO
        // 调用 Mapper 获取本月真实数据
        dto.setMonthlyUsers(usersMapper.getMonthlyUsersCount());
        dto.setMonthlyPosts(communityPostsMapper.getMonthlyPostsCount());
        return dto;
    }
}
