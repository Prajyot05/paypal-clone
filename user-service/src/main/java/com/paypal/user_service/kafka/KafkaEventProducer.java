package com.paypal.user_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.common.dto.UserCreatedEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaEventProducer {

    private static final String TOPIC = "user-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserCreatedEvent(String key, UserCreatedEvent event) {
        System.out.println("📤 Sending to Kafka → Topic: " + TOPIC + ", Key: " + key + ", Message: " + event);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(TOPIC, key, event);

        future.thenAccept(result -> {
            RecordMetadata metadata = result.getRecordMetadata();
            System.out.println("✅ Kafka message sent successfully! Topic: " + metadata.topic() + ", Partition: "
                    + metadata.partition() + ", Offset: " + metadata.offset());
        }).exceptionally(ex -> {
            System.err.println("❌ Failed to send Kafka message: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
    }
}
