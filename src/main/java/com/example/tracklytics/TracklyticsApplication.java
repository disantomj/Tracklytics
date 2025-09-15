package com.example.tracklytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@EnableScheduling
public class TracklyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TracklyticsApplication.class, args);
    }
}
