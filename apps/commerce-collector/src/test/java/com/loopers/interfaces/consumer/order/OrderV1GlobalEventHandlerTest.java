package com.loopers.interfaces.consumer.order;

import com.loopers.domain.auditlog.AuditLogEntity;
import com.loopers.events.GlobalEvent;
import com.loopers.events.commerce.OrderV1Event;
import com.loopers.infrastructure.auditlog.AuditLogJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.KafkaCleanUp;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@SpringBootTest
@TestPropertySource(properties = {
    "spring.kafka.consumer.group-id=commerce-collector",
    "spring.kafka.consumer.heartbeat-interval-ms=1000",
    "spring.kafka.consumer.session-timeout-ms=3000"
})
public class OrderV1GlobalEventHandlerTest {
//    @Autowired
//    KafkaAdmin kafkaAdmin;
//    @Autowired
//    KafkaCleanUp kafkaCleanUp;
//
//    @Autowired
//    DatabaseCleanUp databaseCleanUp;
//
//    @AfterEach
//    void tearDown() {
//        kafkaCleanUp.truncateAllTopics();
//        databaseCleanUp.truncateAllTables();
//    }
//
//    @Autowired
//    KafkaTemplate<Object, Object> kafkaTemplate;
//
//    @MockitoSpyBean
//    OrderV1GlobalEventHandler orderV1GlobalEventHandler;
//
//    @Autowired
//    AuditLogJpaRepository auditLogJpaRepository;
//
//    Logger log = LoggerFactory.getLogger(OrderV1GlobalEventHandlerTest.class);
//
//    @DisplayName("주문 완료 이벤트 수집")
//    @Nested
//    class HandleCompleted {
//        @DisplayName("카프카 주문 완료 이벤트가 정상 처리된다.")
//        @Test
//        void handleCompleted() throws InterruptedException, ExecutionException, TimeoutException {
//            // given
//            // Consumer가 시작될 때까지 잠시 대기
//            Thread.sleep(2000);
//            OrderV1Event.Completed payload = new OrderV1Event.Completed(
//                    1L,
//                    1L,
//                    1000L,
//                    "PG",
//                    List.of(1L, 2L),
//                    Map.of(1L, 2L, 3L, 1L),
//                    Map.of(1L, 2L, 3L, 2L)
//            );
//            GlobalEvent<OrderV1Event.Completed> event = GlobalEvent.of(
//                    OrderV1Event.TOPIC.COMPLETED,
//                    payload
//            );
//
//            // when
//            var prepare = kafkaTemplate.send(OrderV1Event.TOPIC.COMPLETED, event).get(5, TimeUnit.SECONDS);
//            var metadata = prepare.getRecordMetadata();
//            log.info("Sent message result: topic={}, partition={}, offset={}",
//                    metadata.topic(), metadata.partition(), metadata.offset());
//            assertNotNull(prepare);
//            verify(orderV1GlobalEventHandler, timeout(5000)).handle(any(), any());
//
//            // then
//            Thread.sleep(300);
//            log.info("eventId: {}", event.eventId());
//            var auditLog = auditLogJpaRepository.findByEventId(event.eventId());
//            log.info("log: {}", auditLog);
//            assertTrue(auditLog.isPresent());
//        }
//
//        @DisplayName("중복된 이벤트의 요청에도 멱등적으로 처리된다.")
//        @Test
//        void handleCompleted_idempotent() throws InterruptedException, ExecutionException, TimeoutException {
//            // given
//            // Consumer가 시작될 때까지 잠시 대기
//            Thread.sleep(2000);
//            OrderV1Event.Completed payload = new OrderV1Event.Completed(
//                    1L,
//                    1L,
//                    1000L,
//                    "PG",
//                    List.of(1L, 2L),
//                    Map.of(1L, 2L, 3L, 1L),
//                    Map.of(1L, 2L, 3L, 2L)
//            );
//            GlobalEvent<OrderV1Event.Completed> event = GlobalEvent.of(
//                    OrderV1Event.TOPIC.COMPLETED,
//                    payload
//            );
//
//            // when
//            var prepare1 = kafkaTemplate.send(OrderV1Event.TOPIC.COMPLETED, event).get(5, TimeUnit.SECONDS);
//            var prepare2 = kafkaTemplate.send(OrderV1Event.TOPIC.COMPLETED, event).get(5, TimeUnit.SECONDS);
//            var metadata1 = prepare1.getRecordMetadata();
//            var metadata2 = prepare2.getRecordMetadata();
//            log.info("Sent message1 result: topic={}, partition={}, offset={}",
//                    metadata1.topic(), metadata1.partition(), metadata1.offset());
//            log.info("Sent message2 result: topic={}, partition={}, offset={}",
//                    metadata2.topic(), metadata2.partition(), metadata2.offset());
//            assertNotNull(prepare1);
//            assertNotNull(prepare2);
//            // 핸들러가 메시지를 완전히 처리할 때까지 기다림
//            verify(orderV1GlobalEventHandler, timeout(5000).times(2)).handle(any(), any());
//
//            // then
//            Thread.sleep(300);
//            log.info("eventId: {}", event.eventId());
//            var auditLog = auditLogJpaRepository.findByEventId(event.eventId());
//            List<AuditLogEntity> list = (List<AuditLogEntity>) auditLogJpaRepository.findAll();
//            log.info("all audit logs: {}", list);
//            log.info("log: {}", auditLog);
//            assertTrue(auditLog.isPresent());
//            assertEquals(1, list.size());
//        }
//    }
}
