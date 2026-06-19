package com.paypal.wallet_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.common.dto.TransactionEvent;
import com.paypal.common.dto.UserCreatedEvent;
import com.paypal.wallet_service.service.WalletService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private final WalletService walletService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventConsumer(WalletService walletService, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.walletService = walletService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user-created", groupId = "wallet-group")
    public void handleUserCreated(String message) {
        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            System.out.println("📥 Received user-created event for user: " + event.getUserId());
            walletService.createWallet(event.getUserId());
        } catch (Exception e) {
            System.err.println("❌ Error processing user-created event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "txn-fraud-approved", groupId = "wallet-group")
    public void handleTransactionInitiated(String message) {
        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            System.out.println("📥 Received txn-fraud-approved event for txn: " + event.getTransactionId());

            boolean success = walletService.processTransfer(
                    event.getSenderId(), 
                    event.getReceiverId(), 
                    event.getAmount(), 
                    event.getTransactionId()
            );

            if (success) {
                event.setStatus("COMPLETED");
                kafkaTemplate.send("txn-completed", String.valueOf(event.getTransactionId()), event);
            } else {
                event.setStatus("FAILED");
                event.setFailureReason("Insufficient funds or invalid wallets");
                kafkaTemplate.send("txn-failed", String.valueOf(event.getTransactionId()), event);
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing txn-initiated event: " + e.getMessage());
        }
    }
}
