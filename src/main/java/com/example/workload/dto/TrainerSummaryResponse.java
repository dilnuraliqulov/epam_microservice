package com.example.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerSummaryResponse {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean trainerStatus;
    private List<YearSummaryDto> years;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YearSummaryDto {
        private Integer year;
        private List<MonthSummaryDto> months;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthSummaryDto {
        private Integer month;
        private Integer trainingSummaryDuration;
    }
}

