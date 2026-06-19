package com.paypal.transaction_service.service;

import java.util.List;

import com.paypal.transaction_service.entity.Transaction;

public interface TransactionService {
    Transaction initiateTransfer(Transaction transaction, String idempotencyKey);

    List<Transaction> getAllTransactions();
    
    Transaction getTransactionById(Long id);
    
    List<Transaction> getTransactionsByUserId(Long userId);
}
