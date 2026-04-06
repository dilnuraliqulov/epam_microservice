package com.example.workload.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionIdFilter Unit Tests")
class TransactionIdFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private TransactionIdFilter transactionIdFilter;

    @BeforeEach
    void setUp() {
        transactionIdFilter = new TransactionIdFilter();
        MDC.clear();
    }

    @Nested
    @DisplayName("doFilterInternal Tests")
    class DoFilterInternalTests {

        @Test
        @DisplayName("Should generate new transaction ID when not provided in header")
        void doFilterInternal_ShouldGenerateTransactionId_WhenNotProvided() throws ServletException, IOException {
            when(request.getHeader("X-Transaction-Id")).thenReturn(null);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/api/v1/workload");

            doAnswer(invocation -> {
                String transactionId = MDC.get("transactionId");
                assertThat(transactionId).isNotNull();
                assertThat(transactionId).isNotBlank();
                // Verify it looks like a UUID
                assertThat(UUID.fromString(transactionId)).isNotNull();
                return null;
            }).when(filterChain).doFilter(request, response);

            transactionIdFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response).setHeader(eq("X-Transaction-Id"), anyString());
        }

        @Test
        @DisplayName("Should use existing transaction ID from header")
        void doFilterInternal_ShouldUseExistingTransactionId_WhenProvided() throws ServletException, IOException {
            String existingTransactionId = "test-transaction-123";
            when(request.getHeader("X-Transaction-Id")).thenReturn(existingTransactionId);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/api/v1/workload");

            doAnswer(invocation -> {
                String transactionId = MDC.get("transactionId");
                assertThat(transactionId).isEqualTo(existingTransactionId);
                return null;
            }).when(filterChain).doFilter(request, response);

            transactionIdFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verify(response).setHeader("X-Transaction-Id", existingTransactionId);
        }

        @Test
        @DisplayName("Should clear MDC after filter processing")
        void doFilterInternal_ShouldClearMDC_AfterProcessing() throws ServletException, IOException {
            // Given
            when(request.getHeader("X-Transaction-Id")).thenReturn(null);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/api/v1/workload");

            transactionIdFilter.doFilterInternal(request, response, filterChain);

            assertThat(MDC.get("transactionId")).isNull();
        }

        @Test
        @DisplayName("Should clear MDC even when exception occurs")
        void doFilterInternal_ShouldClearMDC_WhenExceptionOccurs() throws ServletException, IOException {
            when(request.getHeader("X-Transaction-Id")).thenReturn(null);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/api/v1/workload");
            doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

            try {
                transactionIdFilter.doFilterInternal(request, response, filterChain);
            } catch (RuntimeException e) {
            }

            assertThat(MDC.get("transactionId")).isNull();
        }

        @Test
        @DisplayName("Should generate new transaction ID when header is empty string")
        void doFilterInternal_ShouldGenerateTransactionId_WhenHeaderIsEmpty() throws ServletException, IOException {
            when(request.getHeader("X-Transaction-Id")).thenReturn("");
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/api/v1/workload");

            doAnswer(invocation -> {
                String transactionId = MDC.get("transactionId");
                assertThat(transactionId).isNotNull();
                assertThat(transactionId).isNotEmpty();
                return null;
            }).when(filterChain).doFilter(request, response);

            transactionIdFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should add transaction ID to response header")
        void doFilterInternal_ShouldAddTransactionIdToResponseHeader() throws ServletException, IOException {
            String transactionId = "custom-transaction-id";
            when(request.getHeader("X-Transaction-Id")).thenReturn(transactionId);
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/api/v1/workload");

            transactionIdFilter.doFilterInternal(request, response, filterChain);

            verify(response).setHeader("X-Transaction-Id", transactionId);
        }
    }
}

