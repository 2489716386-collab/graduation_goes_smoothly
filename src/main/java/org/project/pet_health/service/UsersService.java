package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.dto.AdminLoginDTO;
import org.project.pet_health.dto.PageInfo;
import org.project.pet_health.dto.UserBanDTO;
import org.project.pet_health.entity.Users;

import java.util.Map;

public interface UsersService extends IService<Users> {
    // 管理员分页模糊查询所有用户
    PageInfo<Users> getAdminUsersPage(Integer pageNum, Integer pageSize, String nickname, Long userId, String role);

    // 执行封禁操作 (跨表事务)
    void banUser(UserBanDTO banDTO);

    //用户端操作
    Users getUserProfile(Long userId);
    void updateUserProfile(Users user, Long userId);

    // PC端管理员账号密码登录
    String adminLogin(AdminLoginDTO loginDTO);

    // 新增：微信小程序登录方法
    String wxLogin(String code);
    String userLogin(AdminLoginDTO loginDTO);

    /**
     * 获取用户的统计数据（动态数、获赞数等）
     * @param userId 用户ID
     * @return 包含统计数据的 Map
     */
    Map<String, Long> getUserStats(Long userId);
}
