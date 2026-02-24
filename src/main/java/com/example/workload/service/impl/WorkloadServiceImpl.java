package com.example.workload.service.impl;

import com.example.workload.dto.TrainerSummaryResponse;
import com.example.workload.dto.WorkloadRequest;
import com.example.workload.entity.MonthlySummary;
import com.example.workload.entity.TrainerWorkload;
import com.example.workload.entity.YearSummary;
import com.example.workload.enums.ActionType;
import com.example.workload.repository.TrainerWorkloadRepository;
import com.example.workload.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceImpl implements WorkloadService {

    private final TrainerWorkloadRepository repository;

    @Override
    @Transactional
    public void processWorkload(WorkloadRequest request) {
        log.debug("Processing workload for trainer: {}, action: {}",
                request.getTrainerUsername(), request.getActionType());

        TrainerWorkload trainerWorkload = repository.findByUsername(request.getTrainerUsername())
                .orElseGet(() -> createNewTrainerWorkload(request));

        // Update trainer info
        trainerWorkload.setFirstName(request.getTrainerFirstName());
        trainerWorkload.setLastName(request.getTrainerLastName());
        trainerWorkload.setIsActive(request.getIsActive());

        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        YearSummary yearSummary = trainerWorkload.getOrCreateYearSummary(year);
        MonthlySummary monthlySummary = yearSummary.getOrCreateMonthSummary(month);

        if (request.getActionType() == ActionType.ADD) {
            monthlySummary.addDuration(request.getTrainingDuration());
            log.info("Added {} hours to trainer {} for {}/{}",
                    request.getTrainingDuration(), request.getTrainerUsername(), month, year);
        } else if (request.getActionType() == ActionType.DELETE) {
            monthlySummary.subtractDuration(request.getTrainingDuration());
            log.info("Subtracted {} hours from trainer {} for {}/{}",
                    request.getTrainingDuration(), request.getTrainerUsername(), month, year);
        }

        repository.save(trainerWorkload);
        log.debug("Workload processed successfully for trainer: {}", request.getTrainerUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerSummaryResponse> getTrainerSummary(String username) {
        log.debug("Getting trainer summary for: {}", username);

        return repository.findByUsername(username)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getMonthlyHours(String username, int year, int month) {
        log.debug("Getting monthly hours for trainer: {}, year: {}, month: {}", username, year, month);

        return repository.findByUsername(username)
                .map(trainer -> {
                    YearSummary yearSummary = trainer.getYearSummary(year);
                    if (yearSummary == null) {
                        return 0;
                    }
                    MonthlySummary monthlySummary = yearSummary.getMonthSummary(month);
                    return monthlySummary != null ? monthlySummary.getTrainingSummaryDuration() : 0;
                });
    }

    private TrainerWorkload createNewTrainerWorkload(WorkloadRequest request) {
        return TrainerWorkload.builder()
                .username(request.getTrainerUsername())
                .firstName(request.getTrainerFirstName())
                .lastName(request.getTrainerLastName())
                .isActive(request.getIsActive())
                .build();
    }

    private TrainerSummaryResponse mapToResponse(TrainerWorkload trainer) {
        return TrainerSummaryResponse.builder()
                .trainerUsername(trainer.getUsername())
                .trainerFirstName(trainer.getFirstName())
                .trainerLastName(trainer.getLastName())
                .trainerStatus(trainer.getIsActive())
                .years(trainer.getYears().stream()
                        .map(yearSummary -> TrainerSummaryResponse.YearSummaryDto.builder()
                                .year(yearSummary.getYear())
                                .months(yearSummary.getMonths().stream()
                                        .map(monthlySummary -> TrainerSummaryResponse.MonthSummaryDto.builder()
                                                .month(monthlySummary.getMonth())
                                                .trainingSummaryDuration(monthlySummary.getTrainingSummaryDuration())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}

