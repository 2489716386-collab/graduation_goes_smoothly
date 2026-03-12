package org.project.pet_health.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.project.pet_health.common.Result;
import org.project.pet_health.entity.User;
import org.project.pet_health.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "测试模块", description = "测试相关接口")
@RestController
public class hello {

    @Value("${text}")
    private Object text;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/test1")
    @Tag(name = "测试1")
    public String test() {
        return "你好";
    }

    @GetMapping("/test2")
    @Tag(name = "测试2")
    public Object test2() {
        return text;
    }

    @GetMapping("/user")
    public List<User> user() {
        List<User> list = userMapper.selectList(null);
        System.out.println("查询到的用户数量: " + list.size());
        System.out.println("用户数据: " + list);
        return list;
    }

    @GetMapping("/sucees")
    public Result<?> succes() {
        return Result.success(userMapper.selectList(null));
    }

    @GetMapping("/error")
    public Result<?> error() {
        return Result.error("失败");
    }


}
