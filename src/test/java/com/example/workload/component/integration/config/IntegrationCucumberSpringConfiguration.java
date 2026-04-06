package com.example.workload.component.integration.config;

import com.example.workload.TrainingWorkloadApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@CucumberContextConfiguration
@SpringBootTest(
        classes = TrainingWorkloadApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles({"h2", "test"})
public class IntegrationCucumberSpringConfiguration {
}