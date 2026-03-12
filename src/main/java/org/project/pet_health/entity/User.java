package org.project.pet_health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    // 2. 主键映射：对应 user_id，自增
    @TableId(type = IdType.AUTO, value = "user_id")
    private Long userId;

    // 3. 普通字段：名字一致时可省略 @TableField
    private String openid;
    private String username;
    private String password;
    private String nickname;

    // 4. 下划线转驼峰：avatar_url → avatarUrl（也可显式指定）
    @TableField("avatar_url")
    private String avatarUrl;

    private String role;
    private Integer status;

    // 5. 时间字段映射
    @TableField("create_time")
    private LocalDateTime createTime;


}
