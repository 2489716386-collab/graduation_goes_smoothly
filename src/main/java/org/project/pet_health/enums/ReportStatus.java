package org.project.pet_health.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 举报处理状态枚举
 */
@Getter
public enum ReportStatus {

    PENDING(0, "待处理"),
    IGNORED(1, "已忽略"),
    DELETED(2, "已删除内容");

    @EnumValue   // 标记数据库存取的值
    @JsonValue   // 标记响应给前端的 JSON 值
    private final Integer value;

    private final String desc;

    ReportStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
