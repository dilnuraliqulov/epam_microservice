package com.example.workload.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MonthlySummary Entity Unit Tests")
class MonthlySummaryTest {

    private MonthlySummary monthlySummary;
    private YearSummary yearSummary;

    @BeforeEach
    void setUp() {
        TrainerWorkload trainerWorkload = TrainerWorkload.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        yearSummary = YearSummary.builder()
                .year(2026)
                .trainerWorkload(trainerWorkload)
                .build();

        monthlySummary = MonthlySummary.builder()
                .month(2)
                .trainingSummaryDuration(10)
                .yearSummary(yearSummary)
                .build();
    }

    @Nested
    @DisplayName("addDuration Tests")
    class AddDurationTests {

        @Test
        @DisplayName("Should add duration to existing value")
        void addDuration_ShouldAddToExistingValue() {

            monthlySummary.addDuration(5);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should handle adding zero duration")
        void addDuration_ShouldHandleZero() {
            monthlySummary.addDuration(0);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should handle adding large duration")
        void addDuration_ShouldHandleLargeDuration() {
            monthlySummary.addDuration(1000);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(1010);
        }

        @Test
        @DisplayName("Should accumulate multiple additions")
        void addDuration_ShouldAccumulateMultipleAdditions() {
            monthlySummary.addDuration(5);
            monthlySummary.addDuration(3);
            monthlySummary.addDuration(2);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("subtractDuration Tests")
    class SubtractDurationTests {

        @Test
        @DisplayName("Should subtract duration from existing value")
        void subtractDuration_ShouldSubtractFromExistingValue() {

            // When
            monthlySummary.subtractDuration(3);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should not go below zero")
        void subtractDuration_ShouldNotGoBelowZero() {

            monthlySummary.subtractDuration(15);

            // Then
            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle subtracting exact amount")
        void subtractDuration_ShouldHandleExactAmount() {
            monthlySummary.subtractDuration(10);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle subtracting zero duration")
        void subtractDuration_ShouldHandleZero() {
            monthlySummary.subtractDuration(0);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should accumulate multiple subtractions")
        void subtractDuration_ShouldAccumulateMultipleSubtractions() {
            monthlySummary.subtractDuration(2);
            monthlySummary.subtractDuration(3);
            monthlySummary.subtractDuration(1);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should handle subtraction after reaching zero")
        void subtractDuration_ShouldHandleSubtractionAfterZero() {
            monthlySummary.subtractDuration(10);
            monthlySummary.subtractDuration(5);

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create monthly summary with all fields")
        void builder_ShouldCreateMonthlySummaryWithAllFields() {
            MonthlySummary summary = MonthlySummary.builder()
                    .id(1L)
                    .month(3)
                    .trainingSummaryDuration(25)
                    .yearSummary(yearSummary)
                    .build();

            assertThat(summary.getId()).isEqualTo(1L);
            assertThat(summary.getMonth()).isEqualTo(3);
            assertThat(summary.getTrainingSummaryDuration()).isEqualTo(25);
            assertThat(summary.getYearSummary()).isEqualTo(yearSummary);
        }

        @Test
        @DisplayName("Should handle zero initial duration")
        void builder_ShouldHandleZeroInitialDuration() {
            MonthlySummary summary = MonthlySummary.builder()
                    .month(1)
                    .trainingSummaryDuration(0)
                    .yearSummary(yearSummary)
                    .build();

            assertThat(summary.getTrainingSummaryDuration()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Combined Operations Tests")
    class CombinedOperationsTests {

        @Test
        @DisplayName("Should handle mixed add and subtract operations")
        void combinedOperations_ShouldHandleMixedOperations() {

            monthlySummary.addDuration(5);    // 15
            monthlySummary.subtractDuration(3); // 12
            monthlySummary.addDuration(8);    // 20
            monthlySummary.subtractDuration(10); // 10

            assertThat(monthlySummary.getTrainingSummaryDuration()).isEqualTo(10);
        }
    }
}

