package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.UserBlacklist;

import java.util.List;

public interface UserBlacklistService extends IService<UserBlacklist> {
    // 分页查询黑名单
    Page<UserBlacklist> getBlacklistPage(Integer pageNum, Integer pageSize, String nickname, Long userId);

    // 批量解封用户 (跨表事务)
    void batchUnbanUsers(List<Long> userIds);
}
