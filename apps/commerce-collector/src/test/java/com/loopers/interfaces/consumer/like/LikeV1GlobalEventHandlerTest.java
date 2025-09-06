package com.loopers.interfaces.consumer.like;

import com.loopers.domain.auditlog.AuditLogEntity;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.LikeV1Event;
import com.loopers.infrastructure.auditlog.AuditLogJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.KafkaCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.consumer.group-id=commerce-collector",
        "spring.kafka.consumer.heartbeat-interval-ms=1000",
        "spring.kafka.consumer.session-timeout-ms=3000"
})
class LikeV1GlobalEventHandlerTest {

    @Autowired
    KafkaAdmin kafkaAdmin;
    @Autowired
    KafkaCleanUp kafkaCleanUp;
    @MockitoSpyBean
    private LikeV1GlobalEventHandler likeV1GlobalEventHandler;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        kafkaCleanUp.truncateAllTopics();
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    AuditLogJpaRepository auditLogJpaRepository;

    Logger log = LoggerFactory.getLogger(LikeV1GlobalEventHandlerTest.class);

    @DisplayName("상품 좋아요 집계 이벤트 수집")
    @Nested
    class HandleLikeUpdated {
        @DisplayName("카프카 상품 좋아요 집계 이벤트가 정상 처리된다.")
        @Test
        void handleLikeUpdated() throws InterruptedException, ExecutionException, TimeoutException {
            // Given
            Thread.sleep(2000);
            var event = new LikeV1Event.Updated(
                    1L,
                    1L,
                    1L
            );
            var globalEvent = GlobalEvent.of(LikeV1Event.TOPIC.UPDATED, event);

            // When
            var prepare = kafkaTemplate.send(LikeV1Event.TOPIC.UPDATED, globalEvent).get(10, TimeUnit.SECONDS);
            var metadata = prepare.getRecordMetadata();
            log.info("Sent message result: topic={}, partition={}, offset={}",
                    metadata.topic(), metadata.partition(), metadata.offset());
            assertNotNull(prepare);
            verify(likeV1GlobalEventHandler, timeout(5000)).handle(anyList(), any());

            // Then
            Thread.sleep(300);
            var logs = (List<AuditLogEntity>) auditLogJpaRepository.findAll();
            assertEquals(1, logs.size());
        }
    }
}
