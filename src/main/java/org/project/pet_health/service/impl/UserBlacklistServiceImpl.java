package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.UserBlacklist;
import org.project.pet_health.entity.Users;
import org.project.pet_health.enums.StatusType;
import org.project.pet_health.mapper.UserBlacklistMapper;
import org.project.pet_health.mapper.UsersMapper;
import org.project.pet_health.service.UserBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserBlacklistServiceImpl extends ServiceImpl<UserBlacklistMapper, UserBlacklist> implements UserBlacklistService {

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public Page<UserBlacklist> getBlacklistPage(Integer pageNum, Integer pageSize, String nickname, Long userId) {
        LambdaQueryWrapper<UserBlacklist> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(UserBlacklist::getUserId, userId);
        }

        // 【关键点】如果前端通过"昵称"搜索黑名单，因为黑名单表没有昵称字段，需要先去 Users 表查对应 ID
        if (StringUtils.hasText(nickname)) {
            List<Users> matchedUsers = usersMapper.selectList(new LambdaQueryWrapper<Users>()
                    .like(Users::getNickname, nickname)
                    .select(Users::getUserId)); // 只查出 ID 节省内存

            if (matchedUsers.isEmpty()) {
                return new Page<>(pageNum, pageSize); // 昵称都不存在，直接返回空分页
            }
            // 提取所有匹配的 ID，放入 in 查询中
            List<Long> matchedIds = matchedUsers.stream().map(Users::getUserId).collect(Collectors.toList());
            wrapper.in(UserBlacklist::getUserId, matchedIds);
        }

        wrapper.orderByDesc(UserBlacklist::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUnbanUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;

        // 1. 从黑名单中移除这些记录
        this.remove(new LambdaQueryWrapper<UserBlacklist>().in(UserBlacklist::getUserId, userIds));

        // 2. 将这些用户在 Users 表中的状态改回 1 (正常)
        usersMapper.update(null, new LambdaUpdateWrapper<Users>()
                .in(Users::getUserId, userIds)
                .set(Users::getStatus, StatusType.Yes));
    }
}
