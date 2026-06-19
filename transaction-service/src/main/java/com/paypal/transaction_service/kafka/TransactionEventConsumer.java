package com.paypal.transaction_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.common.dto.TransactionEvent;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.repository.TransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionEventConsumer {

    private final TransactionRepository repository;
    private final ObjectMapper objectMapper;

    public TransactionEventConsumer(TransactionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "txn-completed", groupId = "transaction-group")
    @Transactional
    public void handleTransactionCompleted(String message) {
        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            System.out.println("✅ Received txn-completed for txn: " + event.getTransactionId());
            
            repository.findById(event.getTransactionId()).ifPresent(txn -> {
                txn.setStatus("SUCCESS");
                repository.save(txn);
            });
        } catch (Exception e) {
            System.err.println("❌ Error processing txn-completed event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "txn-failed", groupId = "transaction-group")
    @Transactional
    public void handleTransactionFailed(String message) {
        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            System.out.println("❌ Received txn-failed for txn: " + event.getTransactionId());
            
            repository.findById(event.getTransactionId()).ifPresent(txn -> {
                txn.setStatus("FAILED");
                txn.setFailureReason(event.getFailureReason());
                repository.save(txn);
            });
        } catch (Exception e) {
            System.err.println("❌ Error processing txn-failed event: " + e.getMessage());
        }
    }
}
