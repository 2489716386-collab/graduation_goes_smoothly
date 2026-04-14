package org.project.pet_health.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记该接口或类仅限管理员访问
 */
@Target({ElementType.METHOD, ElementType.TYPE}) // 可以加在方法上，也可以加在类上
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminAPI {
}
