package com.example.workload.mapper;

import com.example.workload.dto.TrainerSummaryResponse;
import com.example.workload.entity.TrainerWorkloadSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class WorkloadMapper {

    public TrainerSummaryResponse toTrainerSummaryResponse(TrainerWorkloadSummary summary) {
        if (summary == null) {
            return null;
        }

        return TrainerSummaryResponse.builder()
                .trainerUsername(summary.getUsername())
                .trainerFirstName(summary.getFirstName())
                .trainerLastName(summary.getLastName())
                .trainerStatus(summary.getIsActive())
                .years(mapYearSummaries(summary.getYears()))
                .build();
    }

    /**
     * Maps a list of YearSummaryData to YearSummaryDto list.
     *
     * @param years the list of year summary data
     * @return the list of year summary DTOs
     */
    private List<TrainerSummaryResponse.YearSummaryDto> mapYearSummaries(List<TrainerWorkloadSummary.YearSummaryData> years) {
        if (years == null) {
            return List.of();
        }

        return years.stream()
                .map(this::mapYearSummary)
                .collect(Collectors.toList());
    }

    /**
     * Maps a single YearSummaryData to YearSummaryDto.
     *
     * @param yearSummary the year summary data
     * @return the year summary DTO
     */
    private TrainerSummaryResponse.YearSummaryDto mapYearSummary(TrainerWorkloadSummary.YearSummaryData yearSummary) {
        return TrainerSummaryResponse.YearSummaryDto.builder()
                .year(yearSummary.getYear())
                .months(mapMonthSummaries(yearSummary.getMonths()))
                .build();
    }

    /**
     * Maps a list of MonthSummaryData to MonthSummaryDto list.
     *
     * @param months the list of monthly summary data
     * @return the list of month summary DTOs
     */
    private List<TrainerSummaryResponse.MonthSummaryDto> mapMonthSummaries(List<TrainerWorkloadSummary.MonthSummaryData> months) {
        if (months == null) {
            return List.of();
        }

        return months.stream()
                .map(this::mapMonthSummary)
                .collect(Collectors.toList());
    }

    /**
     * Maps a single MonthSummaryData to MonthSummaryDto.
     *
     * @param monthlySummary the monthly summary data
     * @return the month summary DTO
     */
    private TrainerSummaryResponse.MonthSummaryDto mapMonthSummary(TrainerWorkloadSummary.MonthSummaryData monthlySummary) {
        return TrainerSummaryResponse.MonthSummaryDto.builder()
                .month(monthlySummary.getMonth())
                .trainingSummaryDuration(monthlySummary.getTrainingSummaryDuration())
                .build();
    }
}

