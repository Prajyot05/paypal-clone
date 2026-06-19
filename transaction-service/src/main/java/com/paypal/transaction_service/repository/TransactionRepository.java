package com.paypal.transaction_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paypal.transaction_service.entity.Transaction;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
