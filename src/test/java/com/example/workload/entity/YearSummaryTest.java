package com.example.workload.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("YearSummary Entity Unit Tests")
class YearSummaryTest {

    private YearSummary yearSummary;
    private TrainerWorkload trainerWorkload;

    @BeforeEach
    void setUp() {
        trainerWorkload = TrainerWorkload.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .years(new ArrayList<>())
                .build();

        yearSummary = YearSummary.builder()
                .year(2026)
                .trainerWorkload(trainerWorkload)
                .months(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("getMonthSummary Tests")
    class GetMonthSummaryTests {

        @Test
        @DisplayName("Should return month summary when exists")
        void getMonthSummary_ShouldReturnMonthlySummary_WhenExists() {
            MonthlySummary monthlySummary = MonthlySummary.builder()
                    .month(2)
                    .trainingSummaryDuration(10)
                    .yearSummary(yearSummary)
                    .build();
            yearSummary.getMonths().add(monthlySummary);

            MonthlySummary result = yearSummary.getMonthSummary(2);

            assertThat(result).isNotNull();
            assertThat(result.getMonth()).isEqualTo(2);
            assertThat(result.getTrainingSummaryDuration()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should return null when month does not exist")
        void getMonthSummary_ShouldReturnNull_WhenNotExists() {
            MonthlySummary result = yearSummary.getMonthSummary(2);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return correct month when multiple months exist")
        void getMonthSummary_ShouldReturnCorrectMonth_WhenMultipleExist() {
            MonthlySummary january = MonthlySummary.builder()
                    .month(1)
                    .trainingSummaryDuration(5)
                    .yearSummary(yearSummary)
                    .build();
            MonthlySummary february = MonthlySummary.builder()
                    .month(2)
                    .trainingSummaryDuration(10)
                    .yearSummary(yearSummary)
                    .build();
            MonthlySummary march = MonthlySummary.builder()
                    .month(3)
                    .trainingSummaryDuration(15)
                    .yearSummary(yearSummary)
                    .build();

            yearSummary.getMonths().add(january);
            yearSummary.getMonths().add(february);
            yearSummary.getMonths().add(march);

            MonthlySummary result = yearSummary.getMonthSummary(2);

            assertThat(result).isNotNull();
            assertThat(result.getMonth()).isEqualTo(2);
            assertThat(result.getTrainingSummaryDuration()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("getOrCreateMonthSummary Tests")
    class GetOrCreateMonthSummaryTests {

        @Test
        @DisplayName("Should return existing month summary when exists")
        void getOrCreateMonthSummary_ShouldReturnExisting_WhenExists() {
            MonthlySummary existingMonthlySummary = MonthlySummary.builder()
                    .month(2)
                    .trainingSummaryDuration(10)
                    .yearSummary(yearSummary)
                    .build();
            yearSummary.getMonths().add(existingMonthlySummary);

            MonthlySummary result = yearSummary.getOrCreateMonthSummary(2);

            assertThat(result).isSameAs(existingMonthlySummary);
            assertThat(yearSummary.getMonths()).hasSize(1);
        }

        @Test
        @DisplayName("Should create new month summary when does not exist")
        void getOrCreateMonthSummary_ShouldCreateNew_WhenNotExists() {
            // When
            MonthlySummary result = yearSummary.getOrCreateMonthSummary(3);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMonth()).isEqualTo(3);
            assertThat(result.getTrainingSummaryDuration()).isEqualTo(0);
            assertThat(result.getYearSummary()).isSameAs(yearSummary);
            assertThat(yearSummary.getMonths()).hasSize(1);
            assertThat(yearSummary.getMonths().get(0)).isSameAs(result);
        }

        @Test
        @DisplayName("Should not duplicate month summary on multiple calls")
        void getOrCreateMonthSummary_ShouldNotDuplicate_OnMultipleCalls() {
            MonthlySummary result1 = yearSummary.getOrCreateMonthSummary(2);
            MonthlySummary result2 = yearSummary.getOrCreateMonthSummary(2);

            assertThat(result1).isSameAs(result2);
            assertThat(yearSummary.getMonths()).hasSize(1);
        }

        @Test
        @DisplayName("Should create multiple different month summaries")
        void getOrCreateMonthSummary_ShouldCreateMultipleMonths() {
            MonthlySummary january = yearSummary.getOrCreateMonthSummary(1);
            MonthlySummary february = yearSummary.getOrCreateMonthSummary(2);
            MonthlySummary march = yearSummary.getOrCreateMonthSummary(3);
            assertThat(yearSummary.getMonths()).hasSize(3);
            assertThat(january.getMonth()).isEqualTo(1);
            assertThat(february.getMonth()).isEqualTo(2);
            assertThat(march.getMonth()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create year summary with all fields")
        void builder_ShouldCreateYearSummaryWithAllFields() {
            YearSummary summary = YearSummary.builder()
                    .id(1L)
                    .year(2027)
                    .trainerWorkload(trainerWorkload)
                    .build();
            assertThat(summary.getId()).isEqualTo(1L);
            assertThat(summary.getYear()).isEqualTo(2027);
            assertThat(summary.getTrainerWorkload()).isEqualTo(trainerWorkload);
            assertThat(summary.getMonths()).isNotNull();
        }
    }
}

