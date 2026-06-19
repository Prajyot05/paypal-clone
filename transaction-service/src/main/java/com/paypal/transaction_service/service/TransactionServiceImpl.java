package com.paypal.transaction_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.repository.TransactionRepository;
import com.paypal.common.dto.TransactionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;
    private final KafkaEventProducer kafkaEventProducer;

    public TransactionServiceImpl(TransactionRepository repository,
            KafkaEventProducer kafkaEventProducer,
            ObjectMapper objectMapper,
            IdempotencyService idempotencyService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.kafkaEventProducer = kafkaEventProducer;
        this.idempotencyService = idempotencyService;
    }

    @Override
    @Transactional
    public Transaction createTransaction(Transaction request) {
        System.out.println("🚀 Entered createTransaction()");

        String idempotencyKey = request.getIdempotencyKey();
        
        // Idempotency check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            boolean isNew = idempotencyService.checkAndSetIdempotency(idempotencyKey);
            if (!isNew) {
                System.out.println("⚠️ Duplicate request detected for key: " + idempotencyKey);
                Optional<Transaction> existing = repository.findByIdempotencyKey(idempotencyKey);
                if (existing.isPresent()) {
                    return existing.get(); // Return the existing transaction
                }
                // If it's locked in Redis but not in DB yet, it might still be processing. 
                // We'll throw an exception or return a conflict.
                throw new IllegalStateException("Transaction is currently processing");
            }
        } else {
            // Generate a random key if none provided to ensure it doesn't fail unique constraint
            idempotencyKey = java.util.UUID.randomUUID().toString();
            request.setIdempotencyKey(idempotencyKey);
        }

        Transaction transaction = new Transaction();
        transaction.setSenderId(request.getSenderId());
        transaction.setReceiverId(request.getReceiverId());
        transaction.setAmount(request.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("PENDING");
        transaction.setIdempotencyKey(idempotencyKey);

        Transaction saved = repository.save(transaction);
        System.out.println("💾 Saved Transaction from DB: " + saved);

        try {
            TransactionEvent event = new TransactionEvent(
                saved.getId(), 
                saved.getSenderId(), 
                saved.getReceiverId(), 
                saved.getAmount(), 
                "INITIATED", 
                null
            );
            
            kafkaEventProducer.sendTransactionEvent(String.valueOf(saved.getId()), event);
            System.out.println("🚀 Kafka message sent: txn-initiated");
        } catch (Exception e) {
            System.err.println("❌ Failed to send Kafka event: " + e.getMessage());
            saved.setStatus("FAILED");
            saved.setFailureReason("Failed to publish event to message broker");
            repository.save(saved);
        }

        return saved;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

}