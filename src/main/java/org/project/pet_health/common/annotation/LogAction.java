package org.project.pet_health.common.annotation;

import java.lang.annotation.*;

/**
 * 自定义日志注解，贴在Controller方法上即可自动记录日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAction {
    String value() default ""; // 用于填写具体的操作描述，例如 "新增敏感词"
}
