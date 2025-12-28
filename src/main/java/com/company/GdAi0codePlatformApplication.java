package com.company;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.company.mapper")
@SpringBootApplication
public class GdAi0codePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GdAi0codePlatformApplication.class, args);
    }

}
