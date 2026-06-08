package com.example.elder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ElderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElderApplication.class, args);
    }
}

