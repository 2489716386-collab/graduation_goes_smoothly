package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.dto.UserBanDTO;
import org.project.pet_health.entity.Users;

public interface UsersService extends IService<Users> {
    // 管理员分页模糊查询所有用户
    Page<Users> getAdminUsersPage(Integer pageNum, Integer pageSize, String nickname, Long userId);

    // 执行封禁操作 (跨表事务)
    void banUser(UserBanDTO banDTO);
}
