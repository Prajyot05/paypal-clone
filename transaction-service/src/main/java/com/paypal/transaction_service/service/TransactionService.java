package com.paypal.transaction_service.service;

import java.util.List;

import com.paypal.transaction_service.entity.Transaction;

public interface TransactionService {
    Transaction createTransaction(Transaction transaction);

    List<Transaction> getAllTransactions();
    
    Transaction getTransactionById(Long id);
    
    List<Transaction> getTransactionsByUserId(Long userId);
}
