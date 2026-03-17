package org.project.pet_health;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.project.pet_health.entity.Users;
import org.project.pet_health.utils.JwtUtil;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PetHealthApplicationTests {


    @Test
    void testJet(){
        Users users = new Users();
        users.setUsername("manage");
        users.setNickname("manage");
        System.out.println(JwtUtil.generateToken(users));
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3NzYyNDc2NzMsInVzZXJJZCI6bnVsbCwiaWF0IjoxNzczNjU1NjczLCJqdGkiOiJ0b2tlbklkIiwidXNlcm5hbWUiOiJtYW5hZ2UifQ.FWyfKVJXGr4OfeM-4dIWBY72cwQaMV1TCewFHYUUGuE";
        Claims claims = JwtUtil.verifyJwt(token);
    }
}

