package com.paypal.analytics_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AnalyticsKafkaConsumer {

    private final AtomicInteger completedTransactions = new AtomicInteger(0);
    private final AtomicInteger failedTransactions = new AtomicInteger(0);
    private final AtomicInteger fraudBlockedTransactions = new AtomicInteger(0);

    @KafkaListener(topics = "txn-completed", groupId = "analytics-group")
    public void consumeCompleted(String message) {
        int count = completedTransactions.incrementAndGet();
        System.out.println("📊 [Analytics] SUCCESSFUL TRANSACTION. Total Success Volume: " + count);
    }

    @KafkaListener(topics = "txn-failed", groupId = "analytics-group")
    public void consumeFailed(String message) {
        // Simple heuristic: if it fails with a fraud message, count it as fraud
        if (message.contains("Velocity") || message.contains("exceeds")) {
            int count = fraudBlockedTransactions.incrementAndGet();
            System.out.println("🚨 [Analytics] FRAUD BLOCKED. Total Fraud Intercepted: " + count);
        } else {
            int count = failedTransactions.incrementAndGet();
            System.out.println("📉 [Analytics] FAILED TRANSACTION. Total Failed Volume: " + count);
        }
    }
}
