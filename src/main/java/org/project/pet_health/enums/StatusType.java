package org.project.pet_health.enums;

/*
枚举：前端返回普通文字，数据库存储0/1/2/3
降低数据库存储开销
 */

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum StatusType {

    No(0,"封禁"),
    Yes(1,"正常");

    /*
    存到数据库的值
     */
    @EnumValue
    private Integer  key;

    /*
      前端的值
    */
    @JsonValue
    private String name;


    StatusType(Integer key, String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
