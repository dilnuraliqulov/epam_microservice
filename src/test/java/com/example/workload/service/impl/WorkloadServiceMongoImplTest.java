package com.example.workload.service.impl;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.entity.TrainerWorkloadSummary;
import com.example.workload.entity.mongo.MonthSummaryEmbedded;
import com.example.workload.entity.mongo.TrainerWorkloadDocument;
import com.example.workload.entity.mongo.YearSummaryEmbedded;
import com.example.workload.enums.ActionType;
import com.example.workload.repository.mongo.TrainerWorkloadMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadServiceMongoImpl Unit Tests")
class WorkloadServiceMongoImplTest {

    @Mock
    private TrainerWorkloadMongoRepository repository;

    @InjectMocks
    private WorkloadServiceMongoImpl workloadService;

    @Captor
    private ArgumentCaptor<TrainerWorkloadDocument> trainerWorkloadCaptor;

    private WorkloadRequest workloadRequest;
    private TrainerWorkloadDocument existingTrainer;

    @BeforeEach
    void setUp() {
        workloadRequest = WorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 2, 15))
                .trainingDuration(2)
                .actionType(ActionType.ADD)
                .build();

        existingTrainer = TrainerWorkloadDocument.builder()
                .id("mongo-id-123")
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .years(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("processWorkload Tests")
    class ProcessWorkloadTests {

        @Test
        @DisplayName("Should create new trainer workload when trainer does not exist")
        void processWorkload_ShouldCreateNewTrainerWorkload_WhenTrainerDoesNotExist() {
            when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(repository.save(any(TrainerWorkloadDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

            workloadService.processWorkload(workloadRequest);

            verify(repository).save(trainerWorkloadCaptor.capture());
            TrainerWorkloadDocument savedTrainer = trainerWorkloadCaptor.getValue();

            assertThat(savedTrainer.getUsername()).isEqualTo("john.doe");
            assertThat(savedTrainer.getFirstName()).isEqualTo("John");
            assertThat(savedTrainer.getLastName()).isEqualTo("Doe");
            assertThat(savedTrainer.getIsActive()).isTrue();
            assertThat(savedTrainer.getYears()).hasSize(1);

            YearSummaryEmbedded yearSummary = savedTrainer.getYears().get(0);
            assertThat(yearSummary.getYear()).isEqualTo(2026);
            assertThat(yearSummary.getMonths()).hasSize(1);

            MonthSummaryEmbedded monthlySummary = yearSummary.getMonths().get(0);
            assertThat(monthlySummary.getMonth()).isEqualTo(2);
            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should add hours to existing trainer workload")
        void processWorkload_ShouldAddHours_WhenTrainerExists() {
            YearSummaryEmbedded yearSummary = YearSummaryEmbedded.builder()
                    .year(2026)
                    .months(new ArrayList<>())
                    .build();

            MonthSummaryEmbedded monthlySummary = MonthSummaryEmbedded.builder()
                    .month(2)
                    .trainingSummaryDuration(5)
                    .build();

            yearSummary.getMonths().add(monthlySummary);
            existingTrainer.getYears().add(yearSummary);

            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(repository.save(any(TrainerWorkloadDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

            workloadService.processWorkload(workloadRequest);

            verify(repository).save(trainerWorkloadCaptor.capture());
            TrainerWorkloadDocument savedTrainer = trainerWorkloadCaptor.getValue();

            MonthSummaryEmbedded updatedMonthlySummary = savedTrainer.getYearSummary(2026).getMonthSummary(2);
            assertThat(updatedMonthlySummary.getTrainingSummaryDuration()).isEqualTo(7); // 5 + 2
        }

        @Test
        @DisplayName("Should subtract hours when action type is DELETE")
        void processWorkload_ShouldSubtractHours_WhenActionTypeIsDelete() {
            workloadRequest.setActionType(ActionType.DELETE);

            YearSummaryEmbedded yearSummary = YearSummaryEmbedded.builder()
                    .year(2026)
                    .months(new ArrayList<>())
                    .build();

            MonthSummaryEmbedded monthlySummary = MonthSummaryEmbedded.builder()
                    .month(2)
                    .trainingSummaryDuration(5)
                    .build();

            yearSummary.getMonths().add(monthlySummary);
            existingTrainer.getYears().add(yearSummary);

            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(repository.save(any(TrainerWorkloadDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

            workloadService.processWorkload(workloadRequest);

            verify(repository).save(trainerWorkloadCaptor.capture());
            TrainerWorkloadDocument savedTrainer = trainerWorkloadCaptor.getValue();

            MonthSummaryEmbedded updatedMonthlySummary = savedTrainer.getYearSummary(2026).getMonthSummary(2);
            assertThat(updatedMonthlySummary.getTrainingSummaryDuration()).isEqualTo(3); // 5 - 2
        }

        @Test
        @DisplayName("Should not go below zero when subtracting more hours than available")
        void processWorkload_ShouldNotGoBelowZero_WhenSubtractingMoreThanAvailable() {
            workloadRequest.setActionType(ActionType.DELETE);
            workloadRequest.setTrainingDuration(10);

            YearSummaryEmbedded yearSummary = YearSummaryEmbedded.builder()
                    .year(2026)
                    .months(new ArrayList<>())
                    .build();

            MonthSummaryEmbedded monthlySummary = MonthSummaryEmbedded.builder()
                    .month(2)
                    .trainingSummaryDuration(5)
                    .build();

            yearSummary.getMonths().add(monthlySummary);
            existingTrainer.getYears().add(yearSummary);

            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(repository.save(any(TrainerWorkloadDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

            workloadService.processWorkload(workloadRequest);

            verify(repository).save(trainerWorkloadCaptor.capture());
            TrainerWorkloadDocument savedTrainer = trainerWorkloadCaptor.getValue();

            MonthSummaryEmbedded updatedMonthlySummary = savedTrainer.getYearSummary(2026).getMonthSummary(2);
            assertThat(updatedMonthlySummary.getTrainingSummaryDuration()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create new year summary when year does not exist")
        void processWorkload_ShouldCreateNewYearSummary_WhenYearDoesNotExist() {
            workloadRequest.setTrainingDate(LocalDate.of(2027, 3, 15));

            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(repository.save(any(TrainerWorkloadDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

            workloadService.processWorkload(workloadRequest);

            verify(repository).save(trainerWorkloadCaptor.capture());
            TrainerWorkloadDocument savedTrainer = trainerWorkloadCaptor.getValue();

            assertThat(savedTrainer.getYears()).hasSize(1);
            assertThat(savedTrainer.getYearSummary(2027)).isNotNull();
            assertThat(savedTrainer.getYearSummary(2027).getMonthSummary(3).getTrainingSummaryDuration()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should update trainer info with each request")
        void processWorkload_ShouldUpdateTrainerInfo_WithEachRequest() {
            workloadRequest.setTrainerFirstName("Johnny");
            workloadRequest.setIsActive(false);

            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));
            when(repository.save(any(TrainerWorkloadDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

            workloadService.processWorkload(workloadRequest);

            verify(repository).save(trainerWorkloadCaptor.capture());
            TrainerWorkloadDocument savedTrainer = trainerWorkloadCaptor.getValue();

            assertThat(savedTrainer.getFirstName()).isEqualTo("Johnny");
            assertThat(savedTrainer.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("getTrainerSummary Tests")
    class GetTrainerSummaryTests {

        @Test
        @DisplayName("Should return trainer summary when trainer exists")
        void getTrainerSummary_ShouldReturnSummary_WhenTrainerExists() {
            YearSummaryEmbedded yearSummary = YearSummaryEmbedded.builder()
                    .year(2026)
                    .months(new ArrayList<>())
                    .build();

            MonthSummaryEmbedded monthlySummary = MonthSummaryEmbedded.builder()
                    .month(2)
                    .trainingSummaryDuration(10)
                    .build();

            yearSummary.getMonths().add(monthlySummary);
            existingTrainer.getYears().add(yearSummary);

            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            Optional<TrainerWorkloadSummary> result = workloadService.getTrainerSummary("john.doe");

            assertThat(result).isPresent();
            TrainerWorkloadSummary response = result.get();

            assertThat(response.getUsername()).isEqualTo("john.doe");
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getYears()).hasSize(1);
            assertThat(response.getYears().get(0).getYear()).isEqualTo(2026);
            assertThat(response.getYears().get(0).getMonths()).hasSize(1);
            assertThat(response.getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(2);
            assertThat(response.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should return empty when trainer does not exist")
        void getTrainerSummary_ShouldReturnEmpty_WhenTrainerDoesNotExist() {
            when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

            Optional<TrainerWorkloadSummary> result = workloadService.getTrainerSummary("unknown");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getMonthlyHours Tests")
    class GetMonthlyHoursTests {

        @Test
        @DisplayName("Should return monthly hours when trainer and month exist")
        void getMonthlyHours_ShouldReturnHours_WhenTrainerAndMonthExist() {
            YearSummaryEmbedded yearSummary = YearSummaryEmbedded.builder()
                    .year(2026)
                    .months(new ArrayList<>())
                    .build();

            MonthSummaryEmbedded monthlySummary = MonthSummaryEmbedded.builder()
                    .month(2)
                    .trainingSummaryDuration(15)
                    .build();

            yearSummary.getMonths().add(monthlySummary);
            existingTrainer.getYears().add(yearSummary);

            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            Optional<Integer> result = workloadService.getMonthlyHours("john.doe", 2026, 2);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should return zero when year does not exist")
        void getMonthlyHours_ShouldReturnZero_WhenYearDoesNotExist() {
            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            Optional<Integer> result = workloadService.getMonthlyHours("john.doe", 2027, 2);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return zero when month does not exist")
        void getMonthlyHours_ShouldReturnZero_WhenMonthDoesNotExist() {
            YearSummaryEmbedded yearSummary = YearSummaryEmbedded.builder()
                    .year(2026)
                    .months(new ArrayList<>())
                    .build();

            existingTrainer.getYears().add(yearSummary);

            when(repository.findByUsername("john.doe")).thenReturn(Optional.of(existingTrainer));

            Optional<Integer> result = workloadService.getMonthlyHours("john.doe", 2026, 3);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return empty when trainer does not exist")
        void getMonthlyHours_ShouldReturnEmpty_WhenTrainerDoesNotExist() {
            when(repository.findByUsername("unknown")).thenReturn(Optional.empty());

            Optional<Integer> result = workloadService.getMonthlyHours("unknown", 2026, 2);

            assertThat(result).isEmpty();
        }
    }
}

