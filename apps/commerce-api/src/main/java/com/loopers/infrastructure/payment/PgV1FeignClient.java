package com.loopers.infrastructure.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import static com.loopers.support.resilience.ResilienceConstant.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgV1FeignClient {
    private final Request request;
    private final FindOrder findOrder;
    private final FindTransaction findTransaction;

    @CircuitBreaker(name = PG_REQUEST_CB, fallbackMethod = "requestFallback")
    @Retry(name = PG_REQUEST_RT)
    ApiResponse<PgV1Dto.Response.Transaction> request(String userId, PgV1Dto.Request.Transaction request) {
        log.info("PG 결제 요청 시작 - userId: {}, request: {}", userId, request);
        try {
            ApiResponse<PgV1Dto.Response.Transaction> response = this.request.request(userId, request);
            log.info("PG 결제 요청 성공 - userId: {}, response: {}", userId, response);
            return response;
        } catch (Exception e) {
            log.error("PG 결제 요청 실패 - userId: {}, request: {}, error: {}", userId, request, e.getMessage(), e);
            throw e;
        }
    }

    @CircuitBreaker(name = PG_FIND_CB, fallbackMethod = "findOrderFallback")
    ApiResponse<PgV1Dto.Response.Order> findOrder(String orderKey, String userId) {
        log.info("PG 주문 조회 시작 - orderKey: {}, userId: {}", orderKey, userId);
        try {
            ApiResponse<PgV1Dto.Response.Order> response = this.findOrder.findOrder(orderKey, userId);
            log.info("PG 주문 조회 성공 - orderKey: {}, userId: {}, response: {}", orderKey, userId, response);
            return response;
        } catch (Exception e) {
            log.error("PG 주문 조회 실패 - orderKey: {}, userId: {}, error: {}", orderKey, userId, e.getMessage(), e);
            throw e;
        }
    }

    @CircuitBreaker(name = PG_FIND_CB, fallbackMethod = "findTransactionFallback")
    ApiResponse<PgV1Dto.Response.Transaction> findTransaction(String transactionKey, String userId) {
        log.info("PG 거래 조회 시작 - transactionKey: {}, userId: {}", transactionKey, userId);
        try {
            ApiResponse<PgV1Dto.Response.Transaction> response = this.findTransaction.findTransaction(transactionKey, userId);
            log.info("PG 거래 조회 성공 - transactionKey: {}, userId: {}, response: {}", transactionKey, userId, response);
            return response;
        } catch (Exception e) {
            log.error("PG 거래 조회 실패 - transactionKey: {}, userId: {}, error: {}", transactionKey, userId, e.getMessage(), e);
            throw e;
        }
    }

    // Fallback 메서드들
    private ApiResponse<PgV1Dto.Response.Transaction> requestFallback(String userId, PgV1Dto.Request.Transaction request, Exception e) {
        log.error("PG 결제 요청 Circuit Breaker 활성화 - userId: {}, request: {}, error: {}", userId, request, e.getMessage(), e);
        throw new RuntimeException("PG 결제 요청 실패 - Circuit Breaker 활성화", e);
    }

    private ApiResponse<PgV1Dto.Response.Order> findOrderFallback(String orderKey, String userId, Exception e) {
        log.error("PG 주문 조회 Circuit Breaker 활성화 - orderKey: {}, userId: {}, error: {}", orderKey, userId, e.getMessage(), e);
        throw new RuntimeException("PG 주문 조회 실패 - Circuit Breaker 활성화", e);
    }

    private ApiResponse<PgV1Dto.Response.Transaction> findTransactionFallback(String transactionKey, String userId, Exception e) {
        log.error("PG 거래 조회 Circuit Breaker 활성화 - transactionKey: {}, userId: {}, error: {}", transactionKey, userId, e.getMessage(), e);
        throw new RuntimeException("PG 거래 조회 실패 - Circuit Breaker 활성화", e);
    }

    @FeignClient(
            name = "pgClient-request",
            url = "http://localhost:8082/api/v1/payments"
    )
    public interface Request {

        @PostMapping
        ApiResponse<PgV1Dto.Response.Transaction> request(
                @RequestHeader("X-USER-ID") String userId,
                @RequestBody PgV1Dto.Request.Transaction request
        );
    }

    @FeignClient(
            name = "pgClient-findOrder",
            url = "http://localhost:8082/api/v1/payments"
    )
    public interface FindOrder {

        @GetMapping
        ApiResponse<PgV1Dto.Response.Order> findOrder(
                @RequestParam(name = "orderId") String orderKey,
                @RequestHeader("X-USER-ID") String userId
        );
    }

    @FeignClient(
            name = "pgClient-findTransaction",
            url = "http://localhost:8082/api/v1/payments"
    )
    public interface FindTransaction {

        @GetMapping("/{transactionKey}")
        ApiResponse<PgV1Dto.Response.Transaction> findTransaction(
                @PathVariable(name = "transactionKey") String transactionKey,
                @RequestHeader("X-USER-ID") String userId
        );
    }
}

