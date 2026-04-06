package com.example.workload.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("h2")
@EnableJpaRepositories(basePackages = "com.example.workload.repository")
@EntityScan(basePackages = "com.example.workload.entity")
public class JpaConfig {
}
