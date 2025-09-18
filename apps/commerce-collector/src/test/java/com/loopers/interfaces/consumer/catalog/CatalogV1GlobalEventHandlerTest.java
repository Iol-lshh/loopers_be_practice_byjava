package com.loopers.interfaces.consumer.catalog;

import com.loopers.domain.auditlog.AuditLogEntity;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.ProductV1Event;
import com.loopers.infrastructure.auditlog.AuditLogJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.KafkaCleanUp;
import com.loopers.utils.RedisCleanUp;
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

//@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.consumer.group-id=commerce-collector",
        "spring.kafka.consumer.heartbeat-interval-ms=1000",
        "spring.kafka.consumer.session-timeout-ms=3000"
})
class CatalogV1GlobalEventHandlerTest {
//
//
//    @Autowired
//    KafkaAdmin kafkaAdmin;
//    @Autowired
//    KafkaCleanUp kafkaCleanUp;
//    @Autowired
//    DatabaseCleanUp databaseCleanUp;
//    @Autowired
//    RedisCleanUp redisCleanUp;
//
//    @AfterEach
//    void tearDown() {
//        kafkaCleanUp.truncateAllTopics();
//        databaseCleanUp.truncateAllTables();
//        redisCleanUp.truncateAll();
//    }
//
//    @Autowired
//    KafkaTemplate<Object, Object> kafkaTemplate;
//
//    @MockitoSpyBean
//    private CatalogV1GlobalEventHandler catalogV1GlobalEventHandler;
//
//    @Autowired
//    AuditLogJpaRepository auditLogJpaRepository;
//
//    Logger log = LoggerFactory.getLogger(CatalogV1GlobalEventHandlerTest.class);
//
//    @DisplayName("handleOutOfStock")
//    @Nested
//    class HandleOutOfStock {
//
//        @DisplayName("OutOfStock 이벤트를 처리한다")
//        //@Test
//        void handleOutOfStock() throws InterruptedException, ExecutionException, TimeoutException {
//            // Given
//            Thread.sleep(2000);
//            var event = new ProductV1Event.OutOfStock(
//                    1L
//            );
//            var globalEvent = GlobalEvent.of(ProductV1Event.TOPIC.OUT_OF_STOCK, event);
//
//            // When
//            var prepare = kafkaTemplate.send(ProductV1Event.TOPIC.OUT_OF_STOCK, globalEvent).get(10, TimeUnit.SECONDS);
//            var metadata = prepare.getRecordMetadata();
//            log.info("Sent message result: topic={}, partition={}, offset={}",
//                    metadata.topic(), metadata.partition(), metadata.offset());
//            assertNotNull(prepare);
//            verify(catalogV1GlobalEventHandler, timeout(5000)).handleOutOfStock(anyList(), any());
//
//            // Then
//            Thread.sleep(300);
//            var auditLogs = (List<AuditLogEntity>) auditLogJpaRepository.findAll();
//            auditLogs.forEach(auditLog -> {log.info(auditLog.toString());});
//            assertEquals(1, auditLogs.size());
//        }
//    }
//
//    @DisplayName("handleViewed")
//    @Nested
//    class HandleViewed {
//        @DisplayName("Viewed 이벤트를 처리한다")
//        //@Test
//        void handleViewed() throws InterruptedException, ExecutionException, TimeoutException {
//            // Given
//            Thread.sleep(2000);
//            var event = new ProductV1Event.Viewed(
//                    1L
//            );
//            var globalEvent = GlobalEvent.of(ProductV1Event.TOPIC.VIEWED, event);
//
//            // When
//            var prepare = kafkaTemplate.send(ProductV1Event.TOPIC.VIEWED, globalEvent).get(10, TimeUnit.SECONDS);
//            var metadata = prepare.getRecordMetadata();
//            log.info("Sent message result: topic={}, partition={}, offset={}",
//                    metadata.topic(), metadata.partition(), metadata.offset());
//            assertNotNull(prepare);
//            verify(catalogV1GlobalEventHandler, timeout(5000)).handleViewed(anyList(), any());
//
//            // Then
//            Thread.sleep(300);
//            var logs = (List<AuditLogEntity>) auditLogJpaRepository.findAll();
//            assertEquals(1, logs.size());
//        }
//    }

}
