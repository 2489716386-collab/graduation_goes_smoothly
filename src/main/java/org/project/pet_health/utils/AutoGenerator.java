package org.project.pet_health.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 * 适配 Spring Boot 3.2.5 + MyBatis-Plus 3.5.15 + Freemarker
 * 类名：AutoGenerator
 */
public class AutoGenerator {

    // ========== 数据库配置（已匹配你的 application.yml） ==========
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pet_health_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "6666";

    // ========== 项目配置 ==========
    private static final String PROJECT_PATH = System.getProperty("user.dir"); // 自动获取项目根路径
    private static final String BASE_PACKAGE = "org.project.pet_health"; // 基础包名
    private static final String AUTHOR = "weiling"; // 作者名（生成到代码注释）

    // ========== 生成配置（包含你所有表） ==========
    private static final String[] TABLE_NAMES = {
            "admin_logs", "care_knowledge_base", "care_plans", "comments",
            "community_posts", "health_records", "likes", "mood_records",
            "notifications", "pet_breeds", "pets", "sensitive_words",
            "user_blacklist", "users"
    };
    private static final String TABLE_PREFIX = ""; // 表无前缀，留空

    public static void main(String[] args) {
        // 1. 初始化生成器
        FastAutoGenerator.create(DB_URL, DB_USERNAME, DB_PASSWORD)
                // 2. 全局配置
                .globalConfig(builder -> {
                    builder.author(AUTHOR) // 作者
                            .outputDir(PROJECT_PATH + "/src/main/java") // 代码输出目录
                            .enableSwagger() // 生成 OpenAPI 3 注解（适配 Knife4j）
                            .commentDate("yyyy-MM-dd") // 注释日期格式
                            .disableOpenDir(); // 生成后不自动打开文件夹
                })
                // 3. 包配置（匹配你的项目结构）
                .packageConfig(builder -> {
                    builder.parent(BASE_PACKAGE) // 基础包
                            .entity("entity") // 实体类包
                            .mapper("mapper") // Mapper 接口包
                            .service("service") // Service 接口包
                            .serviceImpl("service.impl") // Service 实现包
                            .controller("controller") // Controller 包
                            .xml("mapper.xml") // Mapper XML 包
                            // XML 文件输出到 resources/mapper 目录
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    PROJECT_PATH + "/src/main/resources/mapper"
                            ));
                })
                // 4. 策略配置
                // 3. 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude(TABLE_NAMES)
                            .addTablePrefix(TABLE_PREFIX)
                            // 实体类策略
                            .entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .idType(com.baomidou.mybatisplus.annotation.IdType.AUTO)
                            // ✅ 关键：关闭 Swagger2 注解，改为 OpenAPI 3
                           // .enableSwagger() // 保持开启，但 MP 3.5.15 会自动生成 @Schema 而非 @ApiModelProperty
                            .formatFileName("%sEntity") // 可选：实体类命名格式
                            // Controller 策略
                            .controllerBuilder()
                            .enableRestStyle()
                            .enableHyphenStyle()
                            // Service 策略
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl");
                })
                // 5. 模板引擎（Freemarker）
                .templateEngine(new FreemarkerTemplateEngine())
                // 6. 执行生成
                .execute();

        System.out.println("✅ 所有表的代码生成完成！");
        System.out.println("生成路径：" + PROJECT_PATH + "/src/main/java/" + BASE_PACKAGE.replace(".", "/"));
    }
}
