package com.paypal.transaction_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paypal.transaction_service.entity.Transaction;

import java.util.Optional;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    List<Transaction> findBySenderIdOrReceiverIdOrderByTimestampDesc(Long senderId, Long receiverId);
}
