package com.example.workload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.workload")
public class TrainingWorkloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingWorkloadApplication.class, args);
    }
}