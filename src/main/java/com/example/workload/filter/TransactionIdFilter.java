package com.example.workload.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class TransactionIdFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String transactionId = extractOrGenerateTransactionId(request);

            // Store in MDC for logging
            MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);

            // Add to response header for downstream tracking
            response.setHeader(TRANSACTION_ID_HEADER, transactionId);

            log.debug("Transaction started: {}, endpoint: {} {}",
                    transactionId, request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);

            log.debug("Transaction completed: {}, status: {}",
                    transactionId, response.getStatus());

        } finally {
            // Clear MDC to prevent memory leaks
            MDC.remove(TRANSACTION_ID_MDC_KEY);
        }
    }

    private String extractOrGenerateTransactionId(HttpServletRequest request) {
        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (!StringUtils.hasText(transactionId)) {
            transactionId = UUID.randomUUID().toString();
            log.debug("Generated new transaction ID: {}", transactionId);
        } else {
            log.debug("Using existing transaction ID: {}", transactionId);
        }
        return transactionId;
    }
}

