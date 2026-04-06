package com.example.workload.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Common domain model for trainer workload that abstracts away the underlying database.
 * Used as an intermediate representation between database entities and DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerWorkloadSummary {

    private String username;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private List<YearSummaryData> years;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YearSummaryData {
        private Integer year;
        private List<MonthSummaryData> months;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthSummaryData {
        private Integer month;
        private Integer trainingSummaryDuration;
    }
}

