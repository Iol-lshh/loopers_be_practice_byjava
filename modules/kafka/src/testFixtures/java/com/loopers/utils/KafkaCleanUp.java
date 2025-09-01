package com.loopers.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaCleanUp {

    @Autowired
    private KafkaAdmin kafkaAdmin;

    public void truncateAllTopics() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            // 모든 토픽 조회
            ListTopicsResult listTopicsResult = adminClient.listTopics();
            Collection<String> topicNames = listTopicsResult.names().get(10, TimeUnit.SECONDS);

            // 시스템 토픽 제외 (__로 시작하는 토픽들)
            Collection<String> userTopics = topicNames.stream()
                    .filter(topic -> !topic.startsWith("__"))
                    .toList();

            if (!userTopics.isEmpty()) {
                // 사용자 토픽들 삭제
                DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(userTopics);
                deleteTopicsResult.all().get(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Failed to clean up Kafka topics", e);
        }
    }

    public void createTopicIfNotExists(String topicName, int partitions, short replicationFactor) {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Failed to create Kafka topic: " + topicName, e);
        }
    }
}
