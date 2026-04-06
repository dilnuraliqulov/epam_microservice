package com.example.workload.service;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.entity.TrainerWorkloadSummary;

import java.util.Optional;

public interface WorkloadService {

    void processWorkload(WorkloadRequest request);

    Optional<TrainerWorkloadSummary> getTrainerSummary(String username);

    Optional<Integer> getMonthlyHours(String username, int year, int month);
}

