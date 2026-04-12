package com.visualspider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.visualspider")
public class VisualSpiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisualSpiderApplication.class, args);
    }
}

