package com.paypal.notification_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.common.dto.TransactionEvent;
import com.paypal.common.dto.UserCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final ObjectMapper objectMapper;

    public EmailConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user-created", groupId = "notification-group")
    public void handleUserCreated(String message) {
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            System.out.println("📧 [MOCK EMAIL] Welcome to PayPal Clone, " + event.getName() + " (" + event.getEmail() + ")!");
        } catch (Exception e) {
            System.err.println("❌ Error processing user-created event in notification: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "txn-completed", groupId = "notification-group")
    public void handleTransactionCompleted(String message) {
        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            System.out.println("📧 [MOCK EMAIL] Transaction SUCCESS! ID: " + event.getTransactionId() +
                    " | Sent: $" + event.getAmount() + " from User " + event.getSenderId() + " to User " + event.getReceiverId());
        } catch (Exception e) {
            System.err.println("❌ Error processing txn-completed event in notification: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "txn-failed", groupId = "notification-group")
    public void handleTransactionFailed(String message) {
        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            System.out.println("📧 [MOCK EMAIL] Transaction FAILED! ID: " + event.getTransactionId() +
                    " | Reason: " + event.getFailureReason());
        } catch (Exception e) {
            System.err.println("❌ Error processing txn-failed event in notification: " + e.getMessage());
        }
    }
}
