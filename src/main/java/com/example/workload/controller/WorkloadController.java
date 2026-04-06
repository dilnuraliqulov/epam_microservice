package com.example.workload.controller;

import com.example.workload.dto.TrainerSummaryResponse;
import com.example.workload.dto.WorkloadRequest;
import com.example.workload.mapper.WorkloadMapper;
import com.example.workload.service.WorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/workload")
@RequiredArgsConstructor
@Slf4j
public class WorkloadController {

    private final WorkloadService workloadService;
    private final WorkloadMapper workloadMapper;


    @PostMapping
    public ResponseEntity<Void> processWorkload(@Valid @RequestBody WorkloadRequest request) {
        String transactionId = MDC.get("transactionId");
        log.info("Processing workload request for trainer: {}, action: {}, transactionId: {}",
                request.getTrainerUsername(), request.getActionType(), transactionId);

        workloadService.processWorkload(request);

        log.info("Workload processed successfully for trainer: {}, transactionId: {}",
                request.getTrainerUsername(), transactionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummaryResponse> getTrainerSummary(@PathVariable String username) {
        String transactionId = MDC.get("transactionId");
        log.info("Getting trainer summary for: {}, transactionId: {}", username, transactionId);

        return workloadService.getTrainerSummary(username)
                .map(summary -> {
                    log.info("Trainer summary found for: {}, transactionId: {}", username, transactionId);
                    return ResponseEntity.ok(workloadMapper.toTrainerSummaryResponse(summary));
                })
                .orElseGet(() -> {
                    log.warn("Trainer not found: {}, transactionId: {}", username, transactionId);
                    return ResponseEntity.notFound().build();
                });
    }


    @GetMapping("/{username}/years/{year}/months/{month}")
    public ResponseEntity<Integer> getMonthlyHours(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {
        String transactionId = MDC.get("transactionId");
        log.info("Getting monthly hours for trainer: {}, year: {}, month: {}, transactionId: {}",
                username, year, month, transactionId);

        return workloadService.getMonthlyHours(username, year, month)
                .map(hours -> {
                    log.info("Monthly hours retrieved: {} for trainer: {}, transactionId: {}",
                            hours, username, transactionId);
                    return ResponseEntity.ok(hours);
                })
                .orElseGet(() -> {
                    log.warn("Trainer not found: {}, transactionId: {}", username, transactionId);
                    return ResponseEntity.notFound().build();
                });
    }
}

