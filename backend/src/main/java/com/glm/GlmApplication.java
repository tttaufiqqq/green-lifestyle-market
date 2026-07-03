package com.glm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GlmApplication {
    public static void main(String[] args) {
        SpringApplication.run(GlmApplication.class, args);
    }
}
