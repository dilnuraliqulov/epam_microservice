package com.example.workload.messaging;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.enums.ActionType;
import com.example.workload.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadMessageListener Tests")
class WorkloadMessageListenerTest {

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadMessageListener workloadMessageListener;

    private WorkloadRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = WorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 3, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();
    }

    @Nested
    @DisplayName("receiveWorkloadMessage Tests")
    class ReceiveWorkloadMessageTests {

        @Test
        @DisplayName("Should process workload message successfully")
        void shouldProcessWorkloadMessageSuccessfully() {
            // Given
            doNothing().when(workloadService).processWorkload(validRequest);

            // When
            workloadMessageListener.receiveWorkloadMessage(validRequest, "test-tx-123");

            // Then
            verify(workloadService, times(1)).processWorkload(validRequest);
        }

        @Test
        @DisplayName("Should process message with null transactionId")
        void shouldProcessMessageWithNullTransactionId() {
            // Given
            doNothing().when(workloadService).processWorkload(validRequest);

            // When
            workloadMessageListener.receiveWorkloadMessage(validRequest, null);

            // Then
            verify(workloadService, times(1)).processWorkload(validRequest);
        }

        @Test
        @DisplayName("Should rethrow exception when service fails")
        void shouldRethrowExceptionWhenServiceFails() {
            // Given
            RuntimeException exception = new RuntimeException("Database error");
            doThrow(exception).when(workloadService).processWorkload(validRequest);

            // When & Then
            assertThrows(RuntimeException.class, () ->
                    workloadMessageListener.receiveWorkloadMessage(validRequest, "test-tx-123"));
            verify(workloadService, times(1)).processWorkload(validRequest);
        }

        @Test
        @DisplayName("Should clear MDC after processing")
        void shouldClearMdcAfterProcessing() {
            // Given
            doNothing().when(workloadService).processWorkload(validRequest);

            // When
            workloadMessageListener.receiveWorkloadMessage(validRequest, "test-tx-123");

            // Then
            assertNull(MDC.get("transactionId"));
        }

        @Test
        @DisplayName("Should clear MDC even when exception occurs")
        void shouldClearMdcWhenExceptionOccurs() {
            // Given
            doThrow(new RuntimeException("Error")).when(workloadService).processWorkload(validRequest);

            // When
            try {
                workloadMessageListener.receiveWorkloadMessage(validRequest, "test-tx-123");
            } catch (RuntimeException ignored) {
            }

            // Then
            assertNull(MDC.get("transactionId"));
        }

        @Test
        @DisplayName("Should process DELETE action type")
        void shouldProcessDeleteActionType() {
            // Given
            WorkloadRequest deleteRequest = WorkloadRequest.builder()
                    .trainerUsername("john.doe")
                    .trainerFirstName("John")
                    .trainerLastName("Doe")
                    .isActive(true)
                    .trainingDate(LocalDate.of(2026, 3, 15))
                    .trainingDuration(30)
                    .actionType(ActionType.DELETE)
                    .build();
            doNothing().when(workloadService).processWorkload(deleteRequest);

            // When
            workloadMessageListener.receiveWorkloadMessage(deleteRequest, "test-tx-456");

            // Then
            verify(workloadService, times(1)).processWorkload(deleteRequest);
        }
    }
}

