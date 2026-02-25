package com.example.workload.mapper;

import com.example.workload.dto.TrainerSummaryResponse;
import com.example.workload.entity.MonthlySummary;
import com.example.workload.entity.TrainerWorkload;
import com.example.workload.entity.YearSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
        @DisplayName("Should return null when trainer is null")
        void shouldReturnNullWhenTrainerIsNull() {
            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should map trainer with empty years")
        void shouldMapTrainerWithEmptyYears() {
            TrainerWorkload trainer = TrainerWorkload.builder()
                    .username("john.doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .years(new ArrayList<>())
                    .build();

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(trainer);

            assertThat(result).isNotNull();
            assertThat(result.getTrainerUsername()).isEqualTo("john.doe");
            assertThat(result.getTrainerFirstName()).isEqualTo("John");
            assertThat(result.getTrainerLastName()).isEqualTo("Doe");
            assertThat(result.getTrainerStatus()).isTrue();
            assertThat(result.getYears()).isEmpty();
        }

        @Test
        @DisplayName("Should map trainer with null years list")
        void shouldMapTrainerWithNullYears() {
            TrainerWorkload trainer = TrainerWorkload.builder()
                    .username("john.doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .years(null)
                    .build();

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(trainer);

            assertThat(result).isNotNull();
            assertThat(result.getYears()).isEmpty();
        }

        @Test
        @DisplayName("Should map trainer with complete year and month data")
        void shouldMapTrainerWithCompleteData() {
            // Build entity structure
            TrainerWorkload trainer = TrainerWorkload.builder()
                    .username("john.doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .years(new ArrayList<>())
                    .build();

            YearSummary yearSummary = YearSummary.builder()
                    .year(2026)
                    .trainerWorkload(trainer)
                    .months(new ArrayList<>())
                    .build();

            MonthlySummary monthlySummary1 = MonthlySummary.builder()
                    .month(1)
                    .trainingSummaryDuration(10)
                    .yearSummary(yearSummary)
                    .build();

            MonthlySummary monthlySummary2 = MonthlySummary.builder()
                    .month(2)
                    .trainingSummaryDuration(15)
                    .yearSummary(yearSummary)
                    .build();

            yearSummary.getMonths().add(monthlySummary1);
            yearSummary.getMonths().add(monthlySummary2);
            trainer.getYears().add(yearSummary);

            // Execute
            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(trainer);

            // Verify
            assertThat(result).isNotNull();
            assertThat(result.getTrainerUsername()).isEqualTo("john.doe");
            assertThat(result.getTrainerFirstName()).isEqualTo("John");
            assertThat(result.getTrainerLastName()).isEqualTo("Doe");
            assertThat(result.getTrainerStatus()).isTrue();

            assertThat(result.getYears()).hasSize(1);
            TrainerSummaryResponse.YearSummaryDto yearDto = result.getYears().get(0);
            assertThat(yearDto.getYear()).isEqualTo(2026);

            assertThat(yearDto.getMonths()).hasSize(2);
            assertThat(yearDto.getMonths().get(0).getMonth()).isEqualTo(1);
            assertThat(yearDto.getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(10);
            assertThat(yearDto.getMonths().get(1).getMonth()).isEqualTo(2);
            assertThat(yearDto.getMonths().get(1).getTrainingSummaryDuration()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should map trainer with multiple years")
        void shouldMapTrainerWithMultipleYears() {
            TrainerWorkload trainer = TrainerWorkload.builder()
                    .username("jane.doe")
                    .firstName("Jane")
                    .lastName("Doe")
                    .isActive(false)
                    .years(new ArrayList<>())
                    .build();

            YearSummary year2025 = YearSummary.builder()
                    .year(2025)
                    .trainerWorkload(trainer)
                    .months(new ArrayList<>())
                    .build();
            year2025.getMonths().add(MonthlySummary.builder()
                    .month(12)
                    .trainingSummaryDuration(5)
                    .yearSummary(year2025)
                    .build());

            YearSummary year2026 = YearSummary.builder()
                    .year(2026)
                    .trainerWorkload(trainer)
                    .months(new ArrayList<>())
                    .build();
            year2026.getMonths().add(MonthlySummary.builder()
                    .month(1)
                    .trainingSummaryDuration(8)
                    .yearSummary(year2026)
                    .build());

            trainer.getYears().add(year2025);
            trainer.getYears().add(year2026);

            // Execute
            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(trainer);

            // Verify
            assertThat(result).isNotNull();
            assertThat(result.getTrainerStatus()).isFalse();
            assertThat(result.getYears()).hasSize(2);
            assertThat(result.getYears().get(0).getYear()).isEqualTo(2025);
            assertThat(result.getYears().get(1).getYear()).isEqualTo(2026);
        }

        @Test
        @DisplayName("Should handle year with null months")
        void shouldHandleYearWithNullMonths() {
            TrainerWorkload trainer = TrainerWorkload.builder()
                    .username("test.user")
                    .firstName("Test")
                    .lastName("User")
                    .isActive(true)
                    .years(new ArrayList<>())
                    .build();

            YearSummary yearSummary = YearSummary.builder()
                    .year(2026)
                    .trainerWorkload(trainer)
                    .months(null)
                    .build();

            trainer.getYears().add(yearSummary);

            TrainerSummaryResponse result = workloadMapper.toTrainerSummaryResponse(trainer);

            assertThat(result).isNotNull();
            assertThat(result.getYears()).hasSize(1);
            assertThat(result.getYears().get(0).getMonths()).isEmpty();
        }
    }
}

