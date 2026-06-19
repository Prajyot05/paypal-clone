package com.paypal.transaction_service.scheduler;

import com.paypal.transaction_service.entity.OutboxEvent;
import com.paypal.transaction_service.repository.OutboxEventRepository;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisherScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaEventProducer kafkaEventProducer;

    public OutboxPublisherScheduler(OutboxEventRepository outboxEventRepository, KafkaEventProducer kafkaEventProducer) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @Scheduled(fixedDelay = 2000) // Poll every 2 seconds
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        
        for (OutboxEvent event : pendingEvents) {
            try {
                // Since our KafkaEventProducer expects typed objects, we'll send the raw JSON via a custom method
                // or we can deserialize it. Actually, sending raw JSON is better for the Outbox.
                kafkaEventProducer.sendRawJson(event.getType(), event.getAggregateId(), event.getPayload());
                
                event.setStatus("SENT");
                outboxEventRepository.save(event);
            } catch (Exception e) {
                System.err.println("Failed to publish outbox event: " + e.getMessage());
                // Will retry on next poll
            }
        }
    }
}
