package org.project.pet_health.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 举报目标类型枚举
 */
@Getter
public enum TargetType {

    POST("post", "动态帖子"),
    COMMENT("comment", "动态评论");

    @EnumValue   // 标记数据库存取的值
    @JsonValue   // 标记响应给前端的 JSON 值
    private final String value;

    private final String desc;

    TargetType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
