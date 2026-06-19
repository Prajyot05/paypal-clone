package com.paypal.transaction_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.service.TransactionService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Transaction transaction) {
        
        transaction.setIdempotencyKey(idempotencyKey);
        Transaction created = service.createTransaction(transaction);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(created);
    }

    @GetMapping("/all")
    public List<Transaction> getAll() {
        return service.getAllTransactions();
    }

}
