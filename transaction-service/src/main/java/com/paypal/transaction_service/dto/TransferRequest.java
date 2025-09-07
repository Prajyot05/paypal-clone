package com.paypal.transaction_service.dto;

public class TransferRequest {
    private Long senderId;
    private Long receiverId;
    private Double amount;

    // All-args constructor
    public TransferRequest(Long senderId, Long receiverId, Double amount) {
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
