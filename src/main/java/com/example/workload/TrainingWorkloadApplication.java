package com.example.workload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.example.workload")
@EnableDiscoveryClient
public class TrainingWorkloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingWorkloadApplication.class, args);
    }
}

