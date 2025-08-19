package com.loopers.module;

import com.loopers.infrastructure.payment.PgActuatorV1FeignClient;
import com.loopers.infrastructure.payment.PgClientTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class PgFeignClientTest {

    @Autowired
    private PgActuatorV1FeignClient pgActuatorV1FeignClient;
    private static final Logger log = LoggerFactory.getLogger(PgFeignClientTest.class);

    @Test
    public void connectionTest(){
        // pg-simulator 켜져 있어야 함. 정상 연결 확인
        var response = pgActuatorV1FeignClient.health();
        log.info(response);
        assertFalse(response.isEmpty());
    }
}

