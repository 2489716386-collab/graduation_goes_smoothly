package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.dto.AdminLoginDTO;
import org.project.pet_health.dto.PageInfo;
import org.project.pet_health.dto.UserBanDTO;
import org.project.pet_health.entity.UserBlacklist;
import org.project.pet_health.entity.Users;
import org.project.pet_health.enums.StatusType;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.mapper.UserBlacklistMapper;
import org.project.pet_health.mapper.UsersMapper;
import org.project.pet_health.service.UsersService;
import org.project.pet_health.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    // 注入黑名单的 Mapper，防止 Service 循环注入
    @Autowired
    private UserBlacklistMapper userBlacklistMapper;

    @Autowired
    private HttpServletRequest request; // 注入 request

    @Override
    public PageInfo<Users> getAdminUsersPage(Integer pageNum, Integer pageSize, String nickname, Long userId,String role) {
// 1. 构建分页对象
        Page<Users> page = new Page<>(pageNum, pageSize);

        // 2. 构建查询条件
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Users::getUserId, userId);
        wrapper.like(StringUtils.hasText(nickname), Users::getNickname, nickname);
        // 关键：增加对 role 的筛选，匹配数据库中的 'admin' 或 'user'
        wrapper.eq(StringUtils.hasText(role), Users::getRole, role);
        wrapper.orderByDesc(Users::getCreateTime);

        // 3. 执行查询
        this.page(page, wrapper);

        // 4. 返回自定义的 PageInfo 对象（确保导入的是上面创建的 dto.PageInfo）
        return new PageInfo<>(page.getTotal(), page.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，保证两步操作要么全成功，要么全失败
    public void banUser(UserBanDTO banDTO) {
        //获取当前操作管理员ID
        Long currentAdminId = (Long) request.getAttribute("currentUserId");
        //被封禁人ID不能等于当前登录人ID
        if (currentAdminId != null && currentAdminId.equals(banDTO.getUserId())) {
            throw new UserException("操作失败：管理员不可以封禁自己的账号！"); // 抛出自定义异常
        }

        // 1. 将用户表状态改为 0 (封禁)
        Users user = new Users();
        user.setUserId(banDTO.getUserId());
        user.setStatus(StatusType.No);
        this.updateById(user);

        // 2. 将用户加入黑名单表
        UserBlacklist blacklist = new UserBlacklist();
        blacklist.setUserId(banDTO.getUserId());
        blacklist.setReason(banDTO.getReason());
        // 计算过期时间：当前时间 + 前端传来的天数
        // 修复：增加非空校验，防止 NPE
        if (banDTO.getBanDays() != null) {
            blacklist.setExpireTime(LocalDateTime.now().plusDays(banDTO.getBanDays()));
        } else {
            // 默认封禁 7 天，或者抛出自定义异常提示参数错误
            blacklist.setExpireTime(LocalDateTime.now().plusDays(7));
        }
        // createTime 字段通常有自动填充，不用手动 set

        userBlacklistMapper.insert(blacklist);
    }

    @Override
    public Users getUserProfile(Long userId) {
        return this.getById(userId);
    }

    @Override
    public void updateUserProfile(Users user, Long userId) {
        // 为了安全，强行把 ID 设置为当前登录用户的 ID，防止修改别人资料
        user.setUserId(userId);
        // 限制用户不能自己修改 status（封禁状态）、role 等敏感字段
        user.setStatus(null);
        this.updateById(user);
    }

    @Override
    public String adminLogin(AdminLoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        Users admin = this.getOne(new LambdaQueryWrapper<Users>()
                .eq(Users::getUsername, loginDTO.getUsername())
                .last("limit 1")
        );

        // 2. 账号是否存在
        if (admin == null) {
            // 【优化】换成自定义的 UserException
            throw new UserException("管理员账号不存在！");
        }

        // 3. 校验密码
        if (!loginDTO.getPassword().equals(admin.getPassword())) {
            throw new UserException("密码错误！");
        }

        // 4. 校验权限
        if (admin.getRole().equals("user")) {
            throw new UserException("无权访问：该账号不是管理员账号！");
        }

        // 5. 校验账号状态是否被封禁
        if (admin.getStatus() != null && admin.getStatus().equals(StatusType.No)) {
            throw new UserException("该管理员账号已被冻结！");
        }

        // 6. 校验全数通过，签发 Token
        return JwtUtil.generateToken(admin);
    }
}
