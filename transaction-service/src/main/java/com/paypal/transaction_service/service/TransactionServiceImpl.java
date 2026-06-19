package com.paypal.transaction_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.entity.OutboxEvent;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.repository.TransactionRepository;
import com.paypal.transaction_service.repository.OutboxEventRepository;
import com.paypal.transaction_service.client.UserValidationClient;
import com.paypal.common.dto.TransactionEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;
    private final KafkaEventProducer kafkaEventProducer;
    private final UserValidationClient userValidationClient;

    public TransactionServiceImpl(TransactionRepository repository,
            OutboxEventRepository outboxEventRepository,
            KafkaEventProducer kafkaEventProducer,
            ObjectMapper objectMapper,
            IdempotencyService idempotencyService,
            UserValidationClient userValidationClient) {
        this.repository = repository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.kafkaEventProducer = kafkaEventProducer;
        this.idempotencyService = idempotencyService;
        this.userValidationClient = userValidationClient;
    }

    // createTransaction was removed in favor of initiateTransfer.

    @Override
    @Transactional
    @CircuitBreaker(name = "userServiceCB", fallbackMethod = "initiateTransferFallback")
    public Transaction initiateTransfer(Transaction transaction, String idempotencyKey) {
        
        // --- 1. IDEMPOTENCY CHECK ---
        // We use a Redis distributed lock to ensure this exact UUID is only processed once.
        // This prevents double-charges if a user clicks the "Send" button twice.
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            boolean isNewRequest = idempotencyService.checkAndSetIdempotency(idempotencyKey);
            if (!isNewRequest) {
                System.out.println("♻️ Idempotency hit! Request " + idempotencyKey + " already processed.");
                throw new RuntimeException("Duplicate request. Already processing.");
            }
        }

        // --- 2. FAST-FAIL VALIDATION VIA gRPC ---
        // Instead of waiting for the Saga to fail asynchronously, we synchronously check
        // if the users exist using our lightning-fast gRPC client.
        boolean isSenderValid = userValidationClient.validateUser(transaction.getSenderId());
        boolean isReceiverValid = userValidationClient.validateUser(transaction.getReceiverId());
        
        if (!isSenderValid || !isReceiverValid) {
            throw new RuntimeException("Validation failed: Sender or Receiver does not exist in the system.");
        }

        if (transaction.getSenderId().equals(transaction.getReceiverId())) {
            throw new RuntimeException("Cannot send money to yourself.");
        }

        // --- 3. SAVE TRANSACTION ---
        // Create the transaction in a PENDING state. The Saga orchestrator will update this later.
        transaction.setStatus("PENDING");
        transaction.setTimestamp(LocalDateTime.now());
        Transaction saved = repository.save(transaction);
        System.out.println("💾 Saved Transaction from DB: " + saved);

        // --- 4. SAGA ORCHESTRATION & OUTBOX PATTERN ---
        // Create the event payload that the Wallet Service will consume.
        TransactionEvent event = new TransactionEvent(
                saved.getId(),
                saved.getSenderId(),
                saved.getReceiverId(),
                saved.getAmount(),
                "INITIATED",
                null
        );

        // Transactional Outbox Pattern: Save to the outbox table instead of sending to Kafka directly.
        // Because this method is @Transactional, the Transaction save and the Outbox save are atomic!
        // If the DB crashes right after this, we won't have sent a ghost message to Kafka.
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                    "Transaction",
                    String.valueOf(saved.getId()),
                    "txn-initiated",
                    payload
            );
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize transaction event to Outbox", e);
        }

        return saved;
    }
    
    /**
     * Fallback method triggered by Resilience4j if the user-service is down or timing out.
     * This prevents our transaction-service from crashing and returns a graceful error to the client.
     */
    public Transaction initiateTransferFallback(Transaction transaction, String idempotencyKey, Throwable t) {
        System.err.println("🛡️ [Circuit Breaker] Fallback triggered! Error: " + t.getMessage());
        throw new RuntimeException("User validation service is currently degraded. Please try again later.", t);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return repository.findBySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId);
    }

}