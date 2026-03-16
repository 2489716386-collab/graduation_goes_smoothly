package org.project.pet_health.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.project.pet_health.exception.UserException;
import org.project.pet_health.utils.JwtUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
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
            // 直接抛出你自定义的异常，交由 GlobalExceptionHandler 统一处理并返回给前端
            throw new UserException(401, "未登录或Token为空，请先登录！");
        }

        // 2. 校验 Token 的合法性和有效期
        Claims claims = JwtUtil.verifyJwt(token);
        if (claims == null) {
            log.warn("token无效或已过期，请求被拦截");
            throw new UserException(401, "Token无效或已过期，请重新登录！");
        } else {
            // 获取用户ID
            Long userId = Long.valueOf(claims.get("userId").toString());
            log.info("已登录，用户ID：{}", userId);

            // 将 userId 放入 request 属性中，方便后续 Controller 随时获取当前操作人的 ID
            request.setAttribute("currentUserId", userId);
            return true; // 校验通过，放行请求
        }
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
