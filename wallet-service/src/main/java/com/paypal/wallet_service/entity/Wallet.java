package com.paypal.wallet_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    private Long userId;

    private BigDecimal balance;

    @Version
    private Long version;

    public Wallet() {}

    public Wallet(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
