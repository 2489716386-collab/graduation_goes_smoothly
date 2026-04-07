package org.project.pet_health.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.project.pet_health.dto.AdminLoginDTO;
import org.project.pet_health.dto.PageInfo;
import org.project.pet_health.dto.UserBanDTO;
import org.project.pet_health.entity.CommunityPosts;
import org.project.pet_health.entity.LikesEntity;
import org.project.pet_health.entity.UserBlacklist;
import org.project.pet_health.entity.Users;
import org.project.pet_health.enums.StatusType;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.mapper.UserBlacklistMapper;
import org.project.pet_health.mapper.UsersMapper;
import org.project.pet_health.service.CommunityPostsService;
import org.project.pet_health.service.UsersService;
import org.project.pet_health.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    // 注入黑名单的 Mapper，防止 Service 循环注入
    @Autowired
    private UserBlacklistMapper userBlacklistMapper;

    @Autowired
    private CommunityPostsService communityPostsService;

    @Autowired
    private org.project.pet_health.mapper.LikesMapper likesMapper;

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
        wrapper.orderByDesc(Users::getUserId);

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

    @Value("${wechat.miniapp.appid}")
    private String appId;

    @Value("${wechat.miniapp.secret}")
    private String appSecret;

    @Override
    public String wxLogin(String code) {
        // 1. 拼接微信官方接口 URL
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId +
                "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";

        // 2. 发送请求换取 openid
        String response = HttpUtil.get(url);
        JSONObject jsonObject = JSONUtil.parseObj(response);
        String openid = jsonObject.getStr("openid");

        if (openid == null) {
            throw new UserException("微信登录失败: " + jsonObject.getStr("errmsg"));
        }

        // 3. 根据 openid 在数据库查找用户
        Users user = this.getOne(new LambdaQueryWrapper<Users>().eq(Users::getOpenid, openid));

// 4. 如果是新用户，自动注册
        if (user == null) {
            user = new Users();
            user.setOpenid(openid);
            user.setNickname("微信用户"); // 默认昵称
            user.setRole("user");       // 默认角色
            // 修改点1：这里使用 Yes，完全匹配你的 StatusType 枚举
            user.setStatus(StatusType.Yes);
            this.save(user);
        }

        // 5. 生成 JWT Token 返回给前端
        // 修改点2：直接调用你 JwtUtil 里的 generateToken 方法，传入 user 实体即可
        return JwtUtil.generateToken(user);
    }

    @Override
    public Map<String, Long> getUserStats(Long userId) {
        // 1. 统计动态数量：直接 count 该用户发的帖子
        long postCount = communityPostsService.count(
                new LambdaQueryWrapper<CommunityPosts>()
                        .eq(CommunityPosts::getUserId, userId)
        );

        // 2. 统计获赞总数
        long likeCount = 0;

        // 先查出该用户发的所有帖子的 ID
        List<CommunityPosts> myPosts = communityPostsService.list(
                new LambdaQueryWrapper<CommunityPosts>()
                        .select(CommunityPosts::getPostId) // 为了性能，只查 ID 字段
                        .eq(CommunityPosts::getUserId, userId)
        );

        // 如果发过帖子，就去点赞表里查这些帖子被点赞的次数
        // 如果发过帖子，就去点赞表里查这些帖子被点赞的次数
        if (myPosts != null && !myPosts.isEmpty()) {
            List<Long> postIds = myPosts.stream()
                    .map(CommunityPosts::getPostId)
                    .collect(Collectors.toList());

            // 👇 这里把 likesService.count 改成 likesMapper.selectCount
            likeCount = likesMapper.selectCount(
                    new LambdaQueryWrapper<LikesEntity>()
                            .in(LikesEntity::getPostId, postIds)
            );
        }

        // 3. 把两个数字装到 Map 里返回
        Map<String, Long> stats = new HashMap<>();
        stats.put("postCount", postCount);
        stats.put("likeCount", likeCount);

        return stats;
    }
}
