package com.loopers.module;

import com.loopers.utils.KafkaCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class KafkaContainerTest {

    private final Logger log = LoggerFactory.getLogger(KafkaContainerTest.class);

    @Autowired
    KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    KafkaCleanUp kafkaCleanUp;

    @AfterEach
    void tearDown() {
        kafkaCleanUp.truncateAllTopics();
    }

    @Test
    void testProduceAndConsume() throws Exception {
        String topic = "demo.internal.topic-v1";
        String testMessage = "hello, kafka!";

        // 메시지 전송
        var result = kafkaTemplate.send(topic, testMessage)
                .get(5, TimeUnit.SECONDS);
        var metadata = result.getRecordMetadata();
        log.info("Sent message result: {}", result);

        // 메시지 전송 확인
        assertEquals(topic, metadata.topic());
        assertTrue(metadata.partition() >= 0);
        assertTrue(metadata.offset() >= 0);

        // 메시지 수신 (KafkaTemplate의 receive 메서드 사용)
        var receivedRecord = kafkaTemplate.receive(topic, metadata.partition(), metadata.offset());
        assertNotNull(receivedRecord, "수신된 메시지가 null입니다");

        // 수신된 메시지 검증
        assertEquals(testMessage, receivedRecord.value(), "전송된 메시지와 수신된 메시지가 일치하지 않습니다");
        assertEquals(metadata.topic(), receivedRecord.topic(), "토픽이 일치하지 않습니다");
        assertEquals(metadata.partition(), receivedRecord.partition(), "파티션이 일치하지 않습니다");
        assertEquals(metadata.offset(), receivedRecord.offset(), "오프셋이 일치하지 않습니다");

        log.info("테스트 완료: 메시지 전송 및 수신 성공");
        log.info("수신된 메시지: {}", receivedRecord.value());
    }
}

