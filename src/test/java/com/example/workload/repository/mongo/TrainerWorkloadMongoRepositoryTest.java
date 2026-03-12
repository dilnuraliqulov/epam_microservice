package com.example.workload.repository.mongo;

import com.example.workload.entity.mongo.MonthSummaryEmbedded;
import com.example.workload.entity.mongo.TrainerWorkloadDocument;
import com.example.workload.entity.mongo.YearSummaryEmbedded;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("mongo")
@Disabled("Requires MongoDB to be running or embedded MongoDB configured")
@DisplayName("TrainerWorkloadMongoRepository Integration Tests")
class TrainerWorkloadMongoRepositoryTest {

    @Autowired
    private TrainerWorkloadMongoRepository repository;

    private TrainerWorkloadDocument testTrainer;

    @BeforeEach
    void setUp() {
        testTrainer = TrainerWorkloadDocument.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .years(new ArrayList<>())
                .build();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find trainer by username")
    void shouldSaveAndFindByUsername() {
        repository.save(testTrainer);

        Optional<TrainerWorkloadDocument> found = repository.findByUsername("john.doe");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john.doe");
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
        assertThat(found.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when trainer not found")
    void shouldReturnEmptyWhenNotFound() {
        Optional<TrainerWorkloadDocument> found = repository.findByUsername("unknown");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check existence by username")
    void shouldCheckExistenceByUsername() {
        repository.save(testTrainer);

        assertThat(repository.existsByUsername("john.doe")).isTrue();
        assertThat(repository.existsByUsername("unknown")).isFalse();
    }

    @Test
    @DisplayName("Should find trainers by first and last name")
    void shouldFindByFirstNameAndLastName() {
        repository.save(testTrainer);

        TrainerWorkloadDocument anotherTrainer = TrainerWorkloadDocument.builder()
                .username("john.smith")
                .firstName("John")
                .lastName("Smith")
                .isActive(true)
                .years(new ArrayList<>())
                .build();
        repository.save(anotherTrainer);

        List<TrainerWorkloadDocument> found = repository.findByFirstNameAndLastName("John", "Doe");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getUsername()).isEqualTo("john.doe");
    }

    @Test
    @DisplayName("Should save trainer with year and month summaries")
    void shouldSaveTrainerWithYearAndMonthSummaries() {
        YearSummaryEmbedded yearSummary = YearSummaryEmbedded.builder()
                .year(2026)
                .months(new ArrayList<>())
                .build();

        MonthSummaryEmbedded monthSummary = MonthSummaryEmbedded.builder()
                .month(2)
                .trainingSummaryDuration(10)
                .build();

        yearSummary.getMonths().add(monthSummary);
        testTrainer.getYears().add(yearSummary);

        repository.save(testTrainer);

        Optional<TrainerWorkloadDocument> found = repository.findByUsername("john.doe");

        assertThat(found).isPresent();
        assertThat(found.get().getYears()).hasSize(1);
        assertThat(found.get().getYears().get(0).getYear()).isEqualTo(2026);
        assertThat(found.get().getYears().get(0).getMonths()).hasSize(1);
        assertThat(found.get().getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(2);
        assertThat(found.get().getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should update existing trainer")
    void shouldUpdateExistingTrainer() {
        repository.save(testTrainer);

        TrainerWorkloadDocument saved = repository.findByUsername("john.doe").orElseThrow();
        saved.setFirstName("Johnny");
        saved.setIsActive(false);
        repository.save(saved);

        Optional<TrainerWorkloadDocument> updated = repository.findByUsername("john.doe");

        assertThat(updated).isPresent();
        assertThat(updated.get().getFirstName()).isEqualTo("Johnny");
        assertThat(updated.get().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should delete trainer")
    void shouldDeleteTrainer() {
        repository.save(testTrainer);
        assertThat(repository.existsByUsername("john.doe")).isTrue();

        repository.delete(testTrainer);

        assertThat(repository.existsByUsername("john.doe")).isFalse();
    }
}

