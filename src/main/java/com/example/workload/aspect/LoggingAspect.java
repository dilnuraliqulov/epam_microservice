package com.example.workload.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Aspect
@Component
@Slf4j
public class LoggingAspect {


    @Pointcut("within(com.example.workload.controller..*)")
    public void controllerMethods() {}


    @Pointcut("within(com.example.workload.service.impl..*)")
    public void serviceMethods() {}


    @Around("controllerMethods()")
    public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        String transactionId = MDC.get("transactionId");
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        // Log incoming request
        log.info("[TRANSACTION] TransactionId: {}, Endpoint: {}, Request: {}",
                transactionId, methodName, sanitizeArgs(args));

        long startTime = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Log successful response
            String status = extractStatus(result);
            log.info("[TRANSACTION] TransactionId: {}, Endpoint: {}, Response: {}, Duration: {}ms",
                    transactionId, methodName, status, duration);

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;

            // Log error response
            log.error("[TRANSACTION] TransactionId: {}, Endpoint: {}, Error: {}, Duration: {}ms",
                    transactionId, methodName, ex.getMessage(), duration);

            throw ex;
        }
    }


    @Around("serviceMethods()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String transactionId = MDC.get("transactionId");
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.debug("[OPERATION] TransactionId: {}, Method: {}, Args: {}",
                transactionId, methodName, sanitizeArgs(args));

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("[OPERATION] TransactionId: {}, Method: {}, Completed in {}ms",
                    transactionId, methodName, duration);

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("[OPERATION] TransactionId: {}, Method: {}, Failed: {}, Duration: {}ms",
                    transactionId, methodName, ex.getMessage(), duration);

            throw ex;
        }
    }

    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.toString(args);
    }

    private String extractStatus(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return String.valueOf(responseEntity.getStatusCode().value());
        }
        return "200 OK";
    }
}

