package com.example.workload.service;

import com.example.workload.dto.TrainerSummaryResponse;
import com.example.workload.dto.WorkloadRequest;

import java.util.Optional;

public interface WorkloadService {


    void processWorkload(WorkloadRequest request);


    Optional<TrainerSummaryResponse> getTrainerSummary(String username);


    Optional<Integer> getMonthlyHours(String username, int year, int month);
}

