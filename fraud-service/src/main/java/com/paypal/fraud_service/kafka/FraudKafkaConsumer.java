package com.paypal.fraud_service.kafka;

import com.paypal.common.dto.TransactionEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class FraudKafkaConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

    public FraudKafkaConsumer(KafkaTemplate<String, Object> kafkaTemplate, StringRedisTemplate redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "txn-initiated", groupId = "fraud-group")
    public void consumeTransactionInitiated(TransactionEvent event) {
        System.out.println("🛡️ [Fraud Service] Analyzing transaction: " + event.getTransactionId());

        String velocityKey = "velocity:" + event.getSenderId();
        
        // --- 1. VELOCITY CHECK (REDIS) ---
        // We use Redis to count how many transactions this user has attempted in the last 60 seconds.
        // Google wants to see how we handle massive scale and abuse prevention.
        Long txCount = redisTemplate.opsForValue().increment(velocityKey);
        if (txCount != null && txCount == 1) {
            redisTemplate.expire(velocityKey, 60, TimeUnit.SECONDS);
        }

        boolean isFraud = false;
        String reason = "";

        if (txCount != null && txCount > 5) {
            isFraud = true;
            reason = "Velocity check failed: Too many transactions in 1 minute.";
        } else if (event.getAmount() != null && event.getAmount().compareTo(new java.math.BigDecimal("10000")) > 0) {
            // Standard static rule for large amounts
            isFraud = true;
            reason = "Amount exceeds $10,000 threshold. Manual review required.";
        }

        // --- 2. SAGA CONTINUATION ---
        // If it's fraud, we publish a failed event and halt the Saga.
        // If it's valid, we publish an approved event, which the Wallet Service will listen to!
        if (isFraud) {
            System.err.println("❌ [Fraud Service] BLOCKED transaction " + event.getTransactionId() + " - Reason: " + reason);
            event.setStatus("FAILED");
            kafkaTemplate.send("txn-failed", String.valueOf(event.getTransactionId()), event);
        } else {
            System.out.println("✅ [Fraud Service] APPROVED transaction " + event.getTransactionId());
            event.setStatus("FRAUD_APPROVED");
            kafkaTemplate.send("txn-fraud-approved", String.valueOf(event.getTransactionId()), event);
        }
    }
}
