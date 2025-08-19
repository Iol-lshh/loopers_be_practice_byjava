package com.loopers.infrastructure.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "pgActuatorClient",
        url = "http://localhost:8083/actuator"
)
public interface PgActuatorV1FeignClient {

    @GetMapping("/health")
    String health();
}
