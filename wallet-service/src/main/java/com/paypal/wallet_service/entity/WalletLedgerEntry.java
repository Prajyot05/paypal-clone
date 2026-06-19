package com.paypal.wallet_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_ledger")
public class WalletLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long walletId;
    
    private Long transactionId; // Reference to the transaction that caused this
    
    private BigDecimal amount; // Positive for credit, negative for debit
    
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    
    private LocalDateTime timestamp;

    public WalletLedgerEntry() {}

    public WalletLedgerEntry(Long walletId, Long transactionId, BigDecimal amount, String type) {
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
