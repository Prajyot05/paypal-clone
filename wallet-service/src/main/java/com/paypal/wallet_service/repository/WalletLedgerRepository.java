package com.paypal.wallet_service.repository;

import com.paypal.wallet_service.entity.WalletLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletLedgerRepository extends JpaRepository<WalletLedgerEntry, Long> {
    List<WalletLedgerEntry> findByWalletIdOrderByTimestampDesc(Long walletId);
}
