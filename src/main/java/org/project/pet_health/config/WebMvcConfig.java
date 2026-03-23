package org.project.pet_health.config;

import org.project.pet_health.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                // 放行（不拦截）白名单路径
                .excludePathPatterns(
                        "/users/login",       // 放行用户登录接口
                        "/users/register",    // 放行用户注册接口
                        "/admins/login",      // 如果有管理员登录接口也放行
                        "/doc.html",          // 放行 Knife4j 接口文档页面
                        "/webjars/**",        // 放行 Swagger 相关静态资源
                        "/v3/api-docs/**",    // 放行 OpenAPI 3 接口数据
                        "/swagger-resources/**",
                        "/swagger-ui/**",
                        "/error"   ,           // 放行 Spring Boot 的默认错误页面

                        //TODO
                        //暂时开放，便于测试
                        "/users/**",
                        "/admin-logs/**" ,
                        "/care-knowledge/**",
                        "/care-plans/**",
                        "/comments/**",
                        "/admin/posts/**",
                        "/health-records/**",
                        "/likes/**",
                        "/mood-records/**",
                        "/notifications/**",
                        "/pet-breeds/**",
                        "/pets/**",
                        "/sensitive-words/**",
                        "/auth/**",
                        "/user-blacklist/**"
                );
    }
}
