package org.project.pet_health.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 系统通知类型枚举
 */
public enum NotificationType {

    SYSTEM_MAINTENANCE(1, "系统维护类"),
    VIOLATION_WARNING(2, "违规提醒类");

    @EnumValue // 告诉 MyBatis-Plus：存入数据库时，使用这个 value 字段（数字 1 或 2）
    private final Integer value;

    private final String desc;

    NotificationType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonValue // 告诉 Spring Boot (Jackson)：返回给前端 JSON 时，直接返回这个数字
    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
