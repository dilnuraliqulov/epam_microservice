package com.example.workload.service;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.entity.TrainerWorkload;

import java.util.Optional;

public interface WorkloadService {

    void processWorkload(WorkloadRequest request);

    Optional<TrainerWorkload> getTrainerSummary(String username);

    Optional<Integer> getMonthlyHours(String username, int year, int month);
}

