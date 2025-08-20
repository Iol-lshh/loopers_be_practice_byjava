package com.loopers.infrastructure.payment;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "pgClient",
        url = "http://localhost:8082/api/v1/payments"
)
public interface PgV1FeignClient {

    @PostMapping
    ApiResponse<PgV1Dto.Response.Transaction> request(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PgV1Dto.Request.Transaction request
    );

    @GetMapping
    ApiResponse<PgV1Dto.Response.Order> findOrder(
            @RequestParam(name = "orderId") String orderKey,
            @RequestHeader("X-USER-ID") String userId
    );

    @GetMapping("/{transactionKey}")
    ApiResponse<PgV1Dto.Response.Transaction> findTransaction(
            @PathVariable(name = "transactionKey") String transactionKey,
            @RequestHeader("X-USER-ID") String userId
    );
}
