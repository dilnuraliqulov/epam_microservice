package com.example.workload.messaging;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.entity.TrainerWorkload;
import com.example.workload.enums.ActionType;
import com.example.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Workload Message Integration Tests")
class WorkloadMessageIntegrationTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Value("${workload.queue.name}")
    private String queueName;

    @BeforeEach
    void setUp() {
        trainerWorkloadRepository.deleteAll();
    }

    @Test
    @DisplayName("Should process ADD workload message from queue")
    void shouldProcessAddWorkloadMessageFromQueue() {
        // Given
        WorkloadRequest request = WorkloadRequest.builder()
                .trainerUsername("integration.test")
                .trainerFirstName("Integration")
                .trainerLastName("Test")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 3, 15))
                .trainingDuration(90)
                .actionType(ActionType.ADD)
                .build();

        // When
        jmsTemplate.convertAndSend(queueName, request, message -> {
            message.setStringProperty("transactionId", "integration-test-tx-001");
            return message;
        });

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<TrainerWorkload> workload = trainerWorkloadRepository
                    .findByUsername("integration.test");
            assertTrue(workload.isPresent());
            assertEquals("Integration", workload.get().getFirstName());
            assertEquals("Test", workload.get().getLastName());
            assertTrue(workload.get().getIsActive());
        });
    }

    @Test
    @DisplayName("Should process DELETE workload message from queue")
    void shouldProcessDeleteWorkloadMessageFromQueue() {
        // Given - First add some hours
        WorkloadRequest addRequest = WorkloadRequest.builder()
                .trainerUsername("delete.test")
                .trainerFirstName("Delete")
                .trainerLastName("Test")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 3, 15))
                .trainingDuration(120)
                .actionType(ActionType.ADD)
                .build();

        jmsTemplate.convertAndSend(queueName, addRequest);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<TrainerWorkload> workload = trainerWorkloadRepository
                    .findByUsername("delete.test");
            assertTrue(workload.isPresent());
        });

        // When - Delete some hours
        WorkloadRequest deleteRequest = WorkloadRequest.builder()
                .trainerUsername("delete.test")
                .trainerFirstName("Delete")
                .trainerLastName("Test")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 3, 15))
                .trainingDuration(30)
                .actionType(ActionType.DELETE)
                .build();

        jmsTemplate.convertAndSend(queueName, deleteRequest);

        // Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<TrainerWorkload> workload = trainerWorkloadRepository
                    .findByUsername("delete.test");
            assertTrue(workload.isPresent());
            // Original 120 - 30 = 90
            int monthlyHours = workload.get()
                    .getYearSummary(2026)
                    .flatMap(y -> y.getMonthSummary(3))
                    .map(m -> m.getTotalDuration())
                    .orElse(0);
            assertEquals(90, monthlyHours);
        });
    }

    @Test
    @DisplayName("Should process multiple messages for same trainer")
    void shouldProcessMultipleMessagesForSameTrainer() {
        // Given
        String username = "multiple.messages";

        // When - Send multiple ADD messages
        for (int i = 1; i <= 3; i++) {
            WorkloadRequest request = WorkloadRequest.builder()
                    .trainerUsername(username)
                    .trainerFirstName("Multiple")
                    .trainerLastName("Messages")
                    .isActive(true)
                    .trainingDate(LocalDate.of(2026, 3, 15))
                    .trainingDuration(60)
                    .actionType(ActionType.ADD)
                    .build();
            jmsTemplate.convertAndSend(queueName, request);
        }

        // Then - Total should be 180 (3 x 60)
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<TrainerWorkload> workload = trainerWorkloadRepository
                    .findByUsername(username);
            assertTrue(workload.isPresent());
            int totalHours = workload.get()
                    .getYearSummary(2026)
                    .flatMap(y -> y.getMonthSummary(3))
                    .map(m -> m.getTotalDuration())
                    .orElse(0);
            assertEquals(180, totalHours);
        });
    }
}

