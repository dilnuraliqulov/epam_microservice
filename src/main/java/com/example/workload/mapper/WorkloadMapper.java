package com.example.workload.mapper;

import com.example.workload.dto.TrainerSummaryResponse;
import com.example.workload.entity.MonthlySummary;
import com.example.workload.entity.TrainerWorkload;
import com.example.workload.entity.YearSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class WorkloadMapper {

    public TrainerSummaryResponse toTrainerSummaryResponse(TrainerWorkload trainer) {
        if (trainer == null) {
            return null;
        }

        return TrainerSummaryResponse.builder()
                .trainerUsername(trainer.getUsername())
                .trainerFirstName(trainer.getFirstName())
                .trainerLastName(trainer.getLastName())
                .trainerStatus(trainer.getIsActive())
                .years(mapYearSummaries(trainer.getYears()))
                .build();
    }

    /**
     * Maps a list of YearSummary entities to YearSummaryDto list.
     *
     * @param years the list of year summary entities
     * @return the list of year summary DTOs
     */
    private List<TrainerSummaryResponse.YearSummaryDto> mapYearSummaries(List<YearSummary> years) {
        if (years == null) {
            return List.of();
        }

        return years.stream()
                .map(this::mapYearSummary)
                .collect(Collectors.toList());
    }

    /**
     * Maps a single YearSummary entity to YearSummaryDto.
     *
     * @param yearSummary the year summary entity
     * @return the year summary DTO
     */
    private TrainerSummaryResponse.YearSummaryDto mapYearSummary(YearSummary yearSummary) {
        return TrainerSummaryResponse.YearSummaryDto.builder()
                .year(yearSummary.getYear())
                .months(mapMonthSummaries(yearSummary.getMonths()))
                .build();
    }

    /**
     * Maps a list of MonthlySummary entities to MonthSummaryDto list.
     *
     * @param months the list of monthly summary entities
     * @return the list of month summary DTOs
     */
    private List<TrainerSummaryResponse.MonthSummaryDto> mapMonthSummaries(List<MonthlySummary> months) {
        if (months == null) {
            return List.of();
        }

        return months.stream()
                .map(this::mapMonthSummary)
                .collect(Collectors.toList());
    }

    /**
     * Maps a single MonthlySummary entity to MonthSummaryDto.
     *
     * @param monthlySummary the monthly summary entity
     * @return the month summary DTO
     */
    private TrainerSummaryResponse.MonthSummaryDto mapMonthSummary(MonthlySummary monthlySummary) {
        return TrainerSummaryResponse.MonthSummaryDto.builder()
                .month(monthlySummary.getMonth())
                .trainingSummaryDuration(monthlySummary.getTrainingSummaryDuration())
                .build();
    }
}

