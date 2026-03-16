package org.project.pet_health.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry.addMapping("/**")
                // 设置允许跨域请求的域名，"*"代表所有域名。
                // 注意：在 Spring Boot 2.4 及以上版本（包括你的 3.x 版本），当 allowCredentials 为 true 时，不能直接使用 allowedOrigins("*")，必须使用 allowedOriginPatterns("*")
                .allowedOriginPatterns("*")
                // 是否允许携带 Cookie (凭证)
                .allowCredentials(true)
                // 设置允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 设置允许的 Header
                .allowedHeaders("*")
                // 跨域允许时间（秒），在此时间内浏览器不会再次发起 OPTIONS 预检请求
                .maxAge(3600);
    }
}
