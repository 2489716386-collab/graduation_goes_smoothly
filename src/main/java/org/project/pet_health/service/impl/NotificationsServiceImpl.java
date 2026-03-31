package org.project.pet_health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.pet_health.entity.Notifications;
import org.project.pet_health.enums.NotificationType;
import org.project.pet_health.mapper.NotificationsMapper;
import org.project.pet_health.service.NotificationsService;
import org.project.pet_health.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class NotificationsServiceImpl extends ServiceImpl<NotificationsMapper, Notifications> implements NotificationsService {

    @Override
    public Page<Notifications> getAdminPage(Integer pageNum, Integer pageSize, String content, String startDate, String endDate, NotificationType type, Integer noticeId) {
        LambdaQueryWrapper<Notifications> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询通知正文
        if (StringUtils.hasText(content)) {
            wrapper.like(Notifications::getContent, content);
        }
        // 按通知类型筛选 (如: 1系统维护, 2违规提醒)
        if (type != null) {
            wrapper.eq(Notifications::getNoticeType, type);
        }
        // 按发送时间段筛选
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            wrapper.between(Notifications::getCreateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        }

        // 按通知ID倒序排列
        wrapper.orderByDesc(Notifications::getNoticeId);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    // ======== 实现新增的方法，专注处理业务逻辑 ========
    @Override
    public List<Notifications> getUserNoticesByToken(String token) {

        // 1. 解析 Token
        Map<String, Object> claims = (Map<String, Object>) JwtUtil.verifyJwt(token);

        // 2. 提取用户 ID
        Object idObj = claims.get("id");
        if (idObj == null) {
            idObj = claims.get("userId");
        }

        // 如果解析不到用户，抛出异常交由外层处理
        if (idObj == null) {
            throw new RuntimeException("无法识别的用户身份，请尝试重新登录");
        }

        Integer userId = Integer.valueOf(idObj.toString());

        // 3. 构造数据库查询条件：发给全体(0) 或 发给自己(userId)
        LambdaQueryWrapper<Notifications> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Notifications::getReceiverId, 0)
                        .or()
                        .eq(Notifications::getReceiverId, userId))
                .orderByDesc(Notifications::getCreateTime);

        // 4. 调用 MyBatis-Plus 提供的 list 方法查询数据库并返回
        return this.list(wrapper);
    }
}
