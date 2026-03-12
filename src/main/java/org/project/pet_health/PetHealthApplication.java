package org.project.pet_health;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.project.pet_health.mapper")
public class PetHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetHealthApplication.class, args);
    }

}
