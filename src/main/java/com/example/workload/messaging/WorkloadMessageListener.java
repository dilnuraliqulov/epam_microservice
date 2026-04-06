package com.example.workload.messaging;

import com.example.workload.dto.WorkloadRequest;
import com.example.workload.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.jms.enabled", havingValue = "true", matchIfMissing = true)
public class WorkloadMessageListener {

    private static final String TRANSACTION_ID_HEADER = "transactionId";
    private static final String TRANSACTION_ID_KEY = "transactionId";

    private final WorkloadService workloadService;

    @JmsListener(destination = "${workload.queue.name:workload.queue}",
                 containerFactory = "jmsListenerContainerFactory")
    public void receiveWorkloadMessage(WorkloadRequest request,
                                       @Header(name = TRANSACTION_ID_HEADER, required = false) String transactionId) {

        // Set transactionId in MDC for logging
        String txId = transactionId != null ? transactionId : UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID_KEY, txId);

        try {
            log.info("Received workload message for trainer: {}, action: {}",
                    request.getTrainerUsername(), request.getActionType());

            workloadService.processWorkload(request);

            log.info("Workload message processed successfully for trainer: {}",
                    request.getTrainerUsername());
        } catch (Exception e) {
            log.error("Error processing workload message for trainer: {}",
                    request.getTrainerUsername(), e);
            throw e; // Re-throw to trigger redelivery/DLQ
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }
}

