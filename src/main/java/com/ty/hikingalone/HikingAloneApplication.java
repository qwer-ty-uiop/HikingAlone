package com.ty.hikingalone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ty.hikingalone.infrastructure.mapper")
public class HikingAloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(HikingAloneApplication.class, args);
    }

}
