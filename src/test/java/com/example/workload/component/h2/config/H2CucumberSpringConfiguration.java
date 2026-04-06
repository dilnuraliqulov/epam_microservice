package com.example.workload.component.h2.config;

import com.example.workload.TrainingWorkloadApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
        classes = TrainingWorkloadApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration," +
                        "org.springframework.boot.actuate.autoconfigure.data.mongo.MongoHealthContributorAutoConfiguration," +
                        "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration," +
                        "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration",
                "spring.data.mongodb.repositories.enabled=false",
                "spring.jpa.open-in-view=false"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles({"h2", "test"})
public class H2CucumberSpringConfiguration {
}