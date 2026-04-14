package org.project.pet_health.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.project.pet_health.common.AdminAPI;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.utils.JwtUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 打印请求信息
        log.info("--------- LoginInterceptor 开始 ---------");
        long startTime = System.currentTimeMillis();
        request.setAttribute("requestStartTime", startTime);

        // OPTIONS请求不做校验 (放行跨域的预检请求)
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURL().toString();
        log.info("接口登录拦截：path：{}", path);

        // 获取 header 的 token 参数
        String token = request.getHeader("token");
        log.info("登录校验开始，token：{}", token);

        // 1. 判断 Token 是否为空
        if (token == null || token.isEmpty()) {
            log.warn("token为空，请求被拦截");
            throw new UserException(401, "未登录或Token为空，请先登录！");
        }

        // 2. 校验 Token 的合法性和有效期
        Claims claims = JwtUtil.verifyJwt(token);
        if (claims == null) {
            log.warn("token无效或已过期，请求被拦截");
            throw new UserException(401, "Token无效或已过期，请重新登录！");
        }

        // 3. 获取角色和用户信息
        Object roleObj = claims.get("role");
        if (roleObj == null) {
            throw new UserException(401, "Token信息不完整（缺少角色信息），请重新登录！");
        }

        String role = roleObj.toString();
        Long userId = Long.valueOf(claims.get("userId").toString());
        log.info("已登录，用户ID：{}，角色：{}", userId, role);

        // 4. 【核心修改】双向权限隔离
        boolean isAdminApi = false;

        // 判断是否为 Controller 中的方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // 检查这个类上面有没有 @AdminAPI 注解
            boolean hasClassAnnotation = handlerMethod.getBeanType().isAnnotationPresent(AdminAPI.class);
            // 检查这个具体的方法上面有没有 @AdminAPI 注解
            boolean hasMethodAnnotation = handlerMethod.hasMethodAnnotation(AdminAPI.class);

            // 只要类或者方法上贴了这个标签，它就是后台接口
            isAdminApi = hasClassAnnotation || hasMethodAnnotation;
        }

        if (isAdminApi) {
            // 强制拦截：如果是管理后台专有接口，普通用户绝对不能进
            if (!"admin".equals(role)) {
                throw new UserException(403, "权限不足：该操作仅限管理员！");
            }
        } else {
            // 公用接口逻辑：不加注解的接口，管理员和用户都可以访问
            // 这样管理员在 PC 端操作时，也能正常获取宠物列表、用户信息等基础数据
            log.info("访问公用/业务接口，放行角色：{}", role);
        }

        // 5. 存入 request 属性供后续使用
        request.setAttribute("currentUserId", userId);
        return true; // 校验通过，放行请求
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        long startTime = (Long) request.getAttribute("requestStartTime");
        log.info("--------- LoginInterceptor 结束 耗时：{} ms ---------", System.currentTimeMillis() - startTime);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("LoginInterceptor 结束");
    }
}
