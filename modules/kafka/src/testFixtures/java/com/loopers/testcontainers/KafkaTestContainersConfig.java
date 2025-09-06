package com.loopers.testcontainers;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class KafkaTestContainersConfig {

    private static final ConfluentKafkaContainer kafkaContainer;

    static {
        kafkaContainer = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                .withEnv("KAFKA_DELETE_TOPIC_ENABLE", "true");

        kafkaContainer.start();

        // Kafka 브로커 정보를 시스템 프로퍼티로 설정
        System.setProperty("spring.kafka.bootstrap-servers",
                String.format("%s:%d", kafkaContainer.getHost(), kafkaContainer.getFirstMappedPort()));

        // 추가 Kafka 설정들 - 실제 애플리케이션과 일치시키기
        System.setProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer");
        System.setProperty("spring.kafka.producer.value-serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        System.setProperty("spring.kafka.consumer.key-deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        System.setProperty("spring.kafka.consumer.value-deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        System.setProperty("spring.kafka.consumer.auto-offset-reset", "earliest");
        System.setProperty("spring.kafka.consumer.group-id", "test-group");
        
        // 테스트에서 토픽 자동 생성 강제 활성화
        System.setProperty("spring.kafka.properties.auto.create.topics.enable", "true");
    }
}
