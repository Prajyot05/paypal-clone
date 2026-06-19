package com.paypal.transaction_service.dto;

import java.math.BigDecimal;

public class TransferRequest {
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;

    // All-args constructor
    public TransferRequest(Long senderId, Long receiverId, BigDecimal amount) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
