package org.project.pet_health.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 社区内容审核状态枚举 (适用于动态和评论)
 */
@Getter
public enum AuditStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "已发布"),
    REJECTED(2, "违规拦截");

    @EnumValue   // 存入数据库的值
    @JsonValue   // 返回给前端的 JSON 值
    private final Integer value;

    private final String desc;

    AuditStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
