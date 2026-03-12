package org.project.pet_health;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import org.junit.jupiter.api.Test;
import org.project.pet_health.entity.User;
import org.project.pet_health.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PetHealthApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void contextLoads() {
        System.out.println("----- 开始查询所有用户 -----");
        List<User> userList = userMapper.selectList(null);
        System.out.println("共查询到 " + userList.size() + " 条数据");
        userList.forEach(System.out::println);

        for (User u : userList) {
            System.out.println("用户ID：" + u.getUserId());
            System.out.println("用户名：" + u.getUsername());
            System.out.println("昵称：" + u.getNickname());
        }
    }
}
