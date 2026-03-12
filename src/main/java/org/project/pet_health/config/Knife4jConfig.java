package org.project.pet_health.config;



import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    /**
     * 配置接口文档基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 文档标题+描述
                .info(new Info()
                        .title("宠物健康管理系统API")
                        .description("宠物健康管理系统的所有接口文档，包含用户、宠物、订单等模块")
                        .version("1.0.0") // 版本号
                        // 联系人信息（可选）
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@example.com")
                                .url("http://example.com")));
    }
}
