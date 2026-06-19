package com.paypal.common.dto;

import java.math.BigDecimal;

public class TransactionEvent {
    private Long transactionId;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
    private String status; // INITIATED, COMPLETED, FAILED
    private String failureReason;

    public TransactionEvent() {}

    public TransactionEvent(Long transactionId, Long senderId, Long receiverId, BigDecimal amount, String status, String failureReason) {
        this.transactionId = transactionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
    }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
