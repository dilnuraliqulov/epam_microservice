package com.example.workload.service.impl;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.entity.MonthlySummary;
import com.example.workload.entity.TrainerWorkload;
import com.example.workload.entity.TrainerWorkloadSummary;
import com.example.workload.entity.YearSummary;
import com.example.workload.enums.ActionType;
import com.example.workload.repository.TrainerWorkloadRepository;
import com.example.workload.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("h2")
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceImpl implements WorkloadService {

    private final TrainerWorkloadRepository repository;

    @Override
    @Transactional
    public void processWorkload(WorkloadRequest request) {
        log.debug("Processing workload for trainer: {}, action: {}",
                request.getTrainerUsername(), request.getActionType());

        TrainerWorkload trainerWorkload = findOrCreateTrainerWorkload(request);
        updateTrainerInfo(trainerWorkload, request);
        updateWorkloadDuration(trainerWorkload, request);
        repository.save(trainerWorkload);

        log.debug("Workload processed successfully for trainer: {}", request.getTrainerUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainerWorkloadSummary> getTrainerSummary(String username) {
        log.debug("Getting trainer summary for: {}", username);
        return repository.findByUsername(username)
                .map(this::convertToSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getMonthlyHours(String username, int year, int month) {
        log.debug("Getting monthly hours for trainer: {}, year: {}, month: {}", username, year, month);

        return repository.findByUsername(username)
                .map(trainer -> calculateMonthlyHours(trainer, year, month));
    }

    private TrainerWorkload findOrCreateTrainerWorkload(WorkloadRequest request) {
        return repository.findByUsername(request.getTrainerUsername())
                .orElseGet(() -> createNewTrainerWorkload(request));
    }

    private TrainerWorkload createNewTrainerWorkload(WorkloadRequest request) {
        log.debug("Creating new trainer workload for: {}", request.getTrainerUsername());
        return TrainerWorkload.builder()
                .username(request.getTrainerUsername())
                .firstName(request.getTrainerFirstName())
                .lastName(request.getTrainerLastName())
                .isActive(request.getIsActive())
                .build();
    }

    private void updateTrainerInfo(TrainerWorkload trainerWorkload, WorkloadRequest request) {
        trainerWorkload.setFirstName(request.getTrainerFirstName());
        trainerWorkload.setLastName(request.getTrainerLastName());
        trainerWorkload.setIsActive(request.getIsActive());
    }

    private void updateWorkloadDuration(TrainerWorkload trainerWorkload, WorkloadRequest request) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        YearSummary yearSummary = trainerWorkload.getOrCreateYearSummary(year);
        MonthlySummary monthlySummary = yearSummary.getOrCreateMonthSummary(month);

        if (request.getActionType() == ActionType.ADD) {
            addWorkloadDuration(monthlySummary, request, month, year);
        } else if (request.getActionType() == ActionType.DELETE) {
            subtractWorkloadDuration(monthlySummary, request, month, year);
        }
    }

    private void addWorkloadDuration(MonthlySummary monthlySummary, WorkloadRequest request, int month, int year) {
        monthlySummary.addDuration(request.getTrainingDuration());
        log.info("Added {} hours to trainer {} for {}/{}",
                request.getTrainingDuration(), request.getTrainerUsername(), month, year);
    }

    private void subtractWorkloadDuration(MonthlySummary monthlySummary, WorkloadRequest request, int month, int year) {
        monthlySummary.subtractDuration(request.getTrainingDuration());
        log.info("Subtracted {} hours from trainer {} for {}/{}",
                request.getTrainingDuration(), request.getTrainerUsername(), month, year);
    }

    private int calculateMonthlyHours(TrainerWorkload trainer, int year, int month) {
        YearSummary yearSummary = trainer.getYearSummary(year);
        if (yearSummary == null) {
            return 0;
        }
        MonthlySummary monthlySummary = yearSummary.getMonthSummary(month);
        return monthlySummary != null ? monthlySummary.getTrainingSummaryDuration() : 0;
    }

    private TrainerWorkloadSummary convertToSummary(TrainerWorkload trainer) {
        return TrainerWorkloadSummary.builder()
                .username(trainer.getUsername())
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .isActive(trainer.getIsActive())
                .years(trainer.getYears().stream()
                        .map(this::convertYearSummary)
                        .collect(Collectors.toList()))
                .build();
    }

    private TrainerWorkloadSummary.YearSummaryData convertYearSummary(YearSummary yearSummary) {
        return TrainerWorkloadSummary.YearSummaryData.builder()
                .year(yearSummary.getYear())
                .months(yearSummary.getMonths().stream()
                        .map(this::convertMonthSummary)
                        .collect(Collectors.toList()))
                .build();
    }

    private TrainerWorkloadSummary.MonthSummaryData convertMonthSummary(MonthlySummary monthlySummary) {
        return TrainerWorkloadSummary.MonthSummaryData.builder()
                .month(monthlySummary.getMonth())
                .trainingSummaryDuration(monthlySummary.getTrainingSummaryDuration())
                .build();
    }
}

