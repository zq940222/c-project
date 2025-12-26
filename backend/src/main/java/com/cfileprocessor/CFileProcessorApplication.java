package com.cfileprocessor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cfileprocessor.repository")
public class CFileProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CFileProcessorApplication.class, args);
    }
}
