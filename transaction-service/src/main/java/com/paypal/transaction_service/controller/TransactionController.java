package com.paypal.transaction_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.service.TransactionService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody Transaction transaction) {
        Transaction created = service.createTransaction(transaction);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/all")
    public List<Transaction> getAll() {
        return service.getAllTransactions();
    }

}
