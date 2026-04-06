package com.example.workload.controller;

import com.example.workload.dto.TrainerSummaryResponse;
import com.example.workload.dto.WorkloadRequest;
import com.example.workload.entity.TrainerWorkloadSummary;
import com.example.workload.enums.ActionType;
import com.example.workload.mapper.WorkloadMapper;
import com.example.workload.security.JwtTokenProvider;
import com.example.workload.service.WorkloadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkloadController.class)
@DisplayName("WorkloadController Unit Tests")
@AutoConfigureMockMvc
class WorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkloadService workloadService;

    @MockBean
    private WorkloadMapper workloadMapper;

    @MockBean
    @SuppressWarnings("unused") // Required for Spring Security context to load
    private JwtTokenProvider jwtTokenProvider;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("POST /api/v1/workload - processWorkload")
    class ProcessWorkloadTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK when workload processed successfully")
        void processWorkload_ShouldReturn200_WhenSuccessful() throws Exception {
            WorkloadRequest request = WorkloadRequest.builder()
                    .trainerUsername("john.doe")
                    .trainerFirstName("John")
                    .trainerLastName("Doe")
                    .isActive(true)
                    .trainingDate(LocalDate.of(2026, 2, 15))
                    .trainingDuration(2)
                    .actionType(ActionType.ADD)
                    .build();

            doNothing().when(workloadService).processWorkload(any(WorkloadRequest.class));

            mockMvc.perform(post("/api/v1/workload")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(workloadService).processWorkload(any(WorkloadRequest.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 when trainer username is blank")
        void processWorkload_ShouldReturn400_WhenUsernameBlank() throws Exception {
            WorkloadRequest request = WorkloadRequest.builder()
                    .trainerUsername("")
                    .trainerFirstName("John")
                    .trainerLastName("Doe")
                    .isActive(true)
                    .trainingDate(LocalDate.of(2026, 2, 15))
                    .trainingDuration(2)
                    .actionType(ActionType.ADD)
                    .build();

            mockMvc.perform(post("/api/v1/workload")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 when training date is null")
        void processWorkload_ShouldReturn400_WhenTrainingDateNull() throws Exception {
            WorkloadRequest request = WorkloadRequest.builder()
                    .trainerUsername("john.doe")
                    .trainerFirstName("John")
                    .trainerLastName("Doe")
                    .isActive(true)
                    .trainingDate(null)
                    .trainingDuration(2)
                    .actionType(ActionType.ADD)
                    .build();

            mockMvc.perform(post("/api/v1/workload")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 when training duration is negative")
        void processWorkload_ShouldReturn400_WhenDurationNegative() throws Exception {
            WorkloadRequest request = WorkloadRequest.builder()
                    .trainerUsername("john.doe")
                    .trainerFirstName("John")
                    .trainerLastName("Doe")
                    .isActive(true)
                    .trainingDate(LocalDate.of(2026, 2, 15))
                    .trainingDuration(-1)
                    .actionType(ActionType.ADD)
                    .build();

            mockMvc.perform(post("/api/v1/workload")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 400 when action type is null")
        void processWorkload_ShouldReturn400_WhenActionTypeNull() throws Exception {
            WorkloadRequest request = WorkloadRequest.builder()
                    .trainerUsername("john.doe")
                    .trainerFirstName("John")
                    .trainerLastName("Doe")
                    .isActive(true)
                    .trainingDate(LocalDate.of(2026, 2, 15))
                    .trainingDuration(2)
                    .actionType(null)
                    .build();

            mockMvc.perform(post("/api/v1/workload")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void processWorkload_ShouldReturn401_WhenNotAuthenticated() throws Exception {
            // Given
            WorkloadRequest request = WorkloadRequest.builder()
                    .trainerUsername("john.doe")
                    .trainerFirstName("John")
                    .trainerLastName("Doe")
                    .isActive(true)
                    .trainingDate(LocalDate.of(2026, 2, 15))
                    .trainingDuration(2)
                    .actionType(ActionType.ADD)
                    .build();

            mockMvc.perform(post("/api/v1/workload")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/workload/{username} - getTrainerSummary")
    class GetTrainerSummaryTests {

        @Test
        @WithMockUser
        @DisplayName("Should return trainer summary when trainer exists")
        void getTrainerSummary_ShouldReturnSummary_WhenTrainerExists() throws Exception {
            // Build TrainerWorkloadSummary domain model
            TrainerWorkloadSummary summary = TrainerWorkloadSummary.builder()
                    .username("john.doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .years(List.of(
                            TrainerWorkloadSummary.YearSummaryData.builder()
                                    .year(2026)
                                    .months(List.of(
                                            TrainerWorkloadSummary.MonthSummaryData.builder()
                                                    .month(2)
                                                    .trainingSummaryDuration(10)
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .build();

            // Build expected response DTO
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .trainerUsername("john.doe")
                    .trainerFirstName("John")
                    .trainerLastName("Doe")
                    .trainerStatus(true)
                    .years(List.of(
                            TrainerSummaryResponse.YearSummaryDto.builder()
                                    .year(2026)
                                    .months(List.of(
                                            TrainerSummaryResponse.MonthSummaryDto.builder()
                                                    .month(2)
                                                    .trainingSummaryDuration(10)
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .build();

            when(workloadService.getTrainerSummary("john.doe")).thenReturn(Optional.of(summary));
            when(workloadMapper.toTrainerSummaryResponse(summary)).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/workload/john.doe"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.trainerUsername").value("john.doe"))
                    .andExpect(jsonPath("$.trainerFirstName").value("John"))
                    .andExpect(jsonPath("$.trainerLastName").value("Doe"))
                    .andExpect(jsonPath("$.trainerStatus").value(true))
                    .andExpect(jsonPath("$.years[0].year").value(2026))
                    .andExpect(jsonPath("$.years[0].months[0].month").value(2))
                    .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(10));

            verify(workloadMapper).toTrainerSummaryResponse(summary);
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when trainer does not exist")
        void getTrainerSummary_ShouldReturn404_WhenTrainerDoesNotExist() throws Exception {
            when(workloadService.getTrainerSummary("unknown")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/workload/unknown"))
                    .andExpect(status().isNotFound());

            verify(workloadMapper, never()).toTrainerSummaryResponse(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/workload/{username}/years/{year}/months/{month} - getMonthlyHours")
    class GetMonthlyHoursTests {

        @Test
        @WithMockUser
        @DisplayName("Should return monthly hours when trainer exists")
        void getMonthlyHours_ShouldReturnHours_WhenTrainerExists() throws Exception {
            when(workloadService.getMonthlyHours("john.doe", 2026, 2)).thenReturn(Optional.of(15));

            mockMvc.perform(get("/api/v1/workload/john.doe/years/2026/months/2"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("15"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return 404 when trainer does not exist")
        void getMonthlyHours_ShouldReturn404_WhenTrainerDoesNotExist() throws Exception {
            when(workloadService.getMonthlyHours("unknown", 2026, 2)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/workload/unknown/years/2026/months/2"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return zero when no trainings for month")
        void getMonthlyHours_ShouldReturnZero_WhenNoTrainings() throws Exception {
            when(workloadService.getMonthlyHours("john.doe", 2026, 3)).thenReturn(Optional.of(0));

            mockMvc.perform(get("/api/v1/workload/john.doe/years/2026/months/3"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("0"));
        }
    }
}

