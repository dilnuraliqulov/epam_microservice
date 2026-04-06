package com.example.workload.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainerWorkload Entity Unit Tests")
class TrainerWorkloadTest {

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
    }

    @Nested
    @DisplayName("getYearSummary Tests")
    class GetYearSummaryTests {

        @Test
        @DisplayName("Should return year summary when exists")
        void getYearSummary_ShouldReturnYearSummary_WhenExists() {
            YearSummary yearSummary = YearSummary.builder()
                    .year(2026)
                    .trainerWorkload(trainerWorkload)
                    .months(new ArrayList<>())
                    .build();
            trainerWorkload.getYears().add(yearSummary);

            YearSummary result = trainerWorkload.getYearSummary(2026);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2026);
        }

        @Test
        @DisplayName("Should return null when year does not exist")
        void getYearSummary_ShouldReturnNull_WhenNotExists() {
            YearSummary result = trainerWorkload.getYearSummary(2026);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return correct year when multiple years exist")
        void getYearSummary_ShouldReturnCorrectYear_WhenMultipleExist() {
            // Given
            YearSummary yearSummary2025 = YearSummary.builder()
                    .year(2025)
                    .trainerWorkload(trainerWorkload)
                    .months(new ArrayList<>())
                    .build();
            YearSummary yearSummary2026 = YearSummary.builder()
                    .year(2026)
                    .trainerWorkload(trainerWorkload)
                    .months(new ArrayList<>())
                    .build();
            trainerWorkload.getYears().add(yearSummary2025);
            trainerWorkload.getYears().add(yearSummary2026);

            YearSummary result = trainerWorkload.getYearSummary(2026);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2026);
        }
    }

    @Nested
    @DisplayName("getOrCreateYearSummary Tests")
    class GetOrCreateYearSummaryTests {

        @Test
        @DisplayName("Should return existing year summary when exists")
        void getOrCreateYearSummary_ShouldReturnExisting_WhenExists() {
            // Given
            YearSummary existingYearSummary = YearSummary.builder()
                    .year(2026)
                    .trainerWorkload(trainerWorkload)
                    .months(new ArrayList<>())
                    .build();
            trainerWorkload.getYears().add(existingYearSummary);

            YearSummary result = trainerWorkload.getOrCreateYearSummary(2026);

            assertThat(result).isSameAs(existingYearSummary);
            assertThat(trainerWorkload.getYears()).hasSize(1);
        }

        @Test
        @DisplayName("Should create new year summary when does not exist")
        void getOrCreateYearSummary_ShouldCreateNew_WhenNotExists() {
            // When
            YearSummary result = trainerWorkload.getOrCreateYearSummary(2026);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2026);
            assertThat(result.getTrainerWorkload()).isSameAs(trainerWorkload);
            assertThat(trainerWorkload.getYears()).hasSize(1);
            assertThat(trainerWorkload.getYears().get(0)).isSameAs(result);
        }

        @Test
        @DisplayName("Should not duplicate year summary on multiple calls")
        void getOrCreateYearSummary_ShouldNotDuplicate_OnMultipleCalls() {
            YearSummary result1 = trainerWorkload.getOrCreateYearSummary(2026);
            YearSummary result2 = trainerWorkload.getOrCreateYearSummary(2026);

            assertThat(result1).isSameAs(result2);
            assertThat(trainerWorkload.getYears()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create trainer workload with all fields")
        void builder_ShouldCreateTrainerWorkloadWithAllFields() {
            TrainerWorkload trainer = TrainerWorkload.builder()
                    .username("jane.doe")
                    .firstName("Jane")
                    .lastName("Doe")
                    .isActive(false)
                    .build();

            assertThat(trainer.getUsername()).isEqualTo("jane.doe");
            assertThat(trainer.getFirstName()).isEqualTo("Jane");
            assertThat(trainer.getLastName()).isEqualTo("Doe");
            assertThat(trainer.getIsActive()).isFalse();
            assertThat(trainer.getYears()).isNotNull();
        }
    }
}

