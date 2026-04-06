package com.example.workload.mapper;

import com.example.workload.dto.TrainerSummaryResponse;
import com.example.workload.entity.TrainerWorkloadSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkloadMapper Unit Tests")
class WorkloadMapperTest {

    private WorkloadMapper workloadMapper;

    @BeforeEach
    void setUp() {
        workloadMapper = new WorkloadMapper();
    }

    @Nested
    @DisplayName("toTrainerSummaryResponse")
    class ToTrainerSummaryResponseTests {

        @Test
        @DisplayName("Should return null when summary is null")
        void shouldReturnNullWhenSummaryIsNull() {
            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should map summary with empty years")
        void shouldMapSummaryWithEmptyYears() {
            TrainerWorkloadSummary summary = TrainerWorkloadSummary.builder()
                    .username("john.doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .years(new ArrayList<>())
                    .build();

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(summary);

            assertThat(result).isNotNull();
            assertThat(result.getTrainerUsername()).isEqualTo("john.doe");
            assertThat(result.getTrainerFirstName()).isEqualTo("John");
            assertThat(result.getTrainerLastName()).isEqualTo("Doe");
            assertThat(result.getTrainerStatus()).isTrue();
            assertThat(result.getYears()).isEmpty();
        }

        @Test
        @DisplayName("Should map summary with null years list")
        void shouldMapSummaryWithNullYears() {
            TrainerWorkloadSummary summary = TrainerWorkloadSummary.builder()
                    .username("john.doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .years(null)
                    .build();

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(summary);

            assertThat(result).isNotNull();
            assertThat(result.getYears()).isEmpty();
        }

        @Test
        @DisplayName("Should map summary with complete year and month data")
        void shouldMapSummaryWithCompleteData() {
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
                                                    .month(1)
                                                    .trainingSummaryDuration(10)
                                                    .build(),
                                            TrainerWorkloadSummary.MonthSummaryData.builder()
                                                    .month(2)
                                                    .trainingSummaryDuration(15)
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .build();

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(summary);

            assertThat(result).isNotNull();
            assertThat(result.getTrainerUsername()).isEqualTo("john.doe");
            assertThat(result.getTrainerFirstName()).isEqualTo("John");
            assertThat(result.getTrainerLastName()).isEqualTo("Doe");
            assertThat(result.getTrainerStatus()).isTrue();

            assertThat(result.getYears()).hasSize(1);
            TrainerSummaryResponse.YearSummaryDto yearDto = result.getYears().getFirst();
            assertThat(yearDto.getYear()).isEqualTo(2026);

            assertThat(yearDto.getMonths()).hasSize(2);
            assertThat(yearDto.getMonths().get(0).getMonth()).isEqualTo(1);
            assertThat(yearDto.getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(10);
            assertThat(yearDto.getMonths().get(1).getMonth()).isEqualTo(2);
            assertThat(yearDto.getMonths().get(1).getTrainingSummaryDuration()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should map summary with multiple years")
        void shouldMapSummaryWithMultipleYears() {
            TrainerWorkloadSummary summary = TrainerWorkloadSummary.builder()
                    .username("jane.doe")
                    .firstName("Jane")
                    .lastName("Doe")
                    .isActive(false)
                    .years(List.of(
                            TrainerWorkloadSummary.YearSummaryData.builder()
                                    .year(2025)
                                    .months(List.of(
                                            TrainerWorkloadSummary.MonthSummaryData.builder()
                                                    .month(12)
                                                    .trainingSummaryDuration(5)
                                                    .build()
                                    ))
                                    .build(),
                            TrainerWorkloadSummary.YearSummaryData.builder()
                                    .year(2026)
                                    .months(List.of(
                                            TrainerWorkloadSummary.MonthSummaryData.builder()
                                                    .month(1)
                                                    .trainingSummaryDuration(8)
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .build();

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(summary);

            assertThat(result).isNotNull();
            assertThat(result.getTrainerUsername()).isEqualTo("jane.doe");
            assertThat(result.getTrainerStatus()).isFalse();
            assertThat(result.getYears()).hasSize(2);

            assertThat(result.getYears().get(0).getYear()).isEqualTo(2025);
            assertThat(result.getYears().get(0).getMonths().getFirst().getMonth()).isEqualTo(12);
            assertThat(result.getYears().get(0).getMonths().getFirst().getTrainingSummaryDuration()).isEqualTo(5);

            assertThat(result.getYears().get(1).getYear()).isEqualTo(2026);
            assertThat(result.getYears().get(1).getMonths().getFirst().getMonth()).isEqualTo(1);
            assertThat(result.getYears().get(1).getMonths().getFirst().getTrainingSummaryDuration()).isEqualTo(8);
        }

        @Test
        @DisplayName("Should map year with empty months")
        void shouldMapYearWithEmptyMonths() {
            TrainerWorkloadSummary summary = TrainerWorkloadSummary.builder()
                    .username("john.doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .years(List.of(
                            TrainerWorkloadSummary.YearSummaryData.builder()
                                    .year(2026)
                                    .months(new ArrayList<>())
                                    .build()
                    ))
                    .build();

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(summary);

            assertThat(result.getYears()).hasSize(1);
            assertThat(result.getYears().getFirst().getMonths()).isEmpty();
        }

        @Test
        @DisplayName("Should map year with null months")
        void shouldMapYearWithNullMonths() {
            TrainerWorkloadSummary summary = TrainerWorkloadSummary.builder()
                    .username("john.doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .years(List.of(
                            TrainerWorkloadSummary.YearSummaryData.builder()
                                    .year(2026)
                                    .months(null)
                                    .build()
                    ))
                    .build();

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(summary);

            assertThat(result.getYears()).hasSize(1);
            assertThat(result.getYears().getFirst().getMonths()).isEmpty();
        }
    }
}

