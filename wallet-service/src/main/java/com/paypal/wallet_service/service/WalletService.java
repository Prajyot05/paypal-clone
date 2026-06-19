package com.paypal.wallet_service.service;

import com.paypal.wallet_service.entity.Wallet;
import com.paypal.wallet_service.entity.WalletLedgerEntry;
import com.paypal.wallet_service.repository.WalletLedgerRepository;
import com.paypal.wallet_service.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletLedgerRepository ledgerRepository;

    public WalletService(WalletRepository walletRepository, WalletLedgerRepository ledgerRepository) {
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional
    public void createWallet(Long userId) {
        if (walletRepository.findById(userId).isEmpty()) {
            Wallet wallet = new Wallet(userId, BigDecimal.ZERO);
            walletRepository.save(wallet);
            System.out.println("✅ Wallet created for user: " + userId);
        }
    }

    @Transactional
    public boolean processTransfer(Long senderId, Long receiverId, BigDecimal amount, Long transactionId) {
        // Must lock accounts in a consistent order to prevent deadlocks
        Long firstLock = senderId < receiverId ? senderId : receiverId;
        Long secondLock = senderId < receiverId ? receiverId : senderId;

        // Obtain pessimistic locks
        walletRepository.findByUserIdForUpdate(firstLock);
        walletRepository.findByUserIdForUpdate(secondLock);

        Wallet senderWallet = walletRepository.findById(senderId).orElse(null);
        Wallet receiverWallet = walletRepository.findById(receiverId).orElse(null);

        if (senderWallet == null || receiverWallet == null) {
            System.err.println("❌ Transfer failed: Sender or receiver wallet not found");
            return false;
        }

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            System.err.println("❌ Transfer failed: Insufficient funds for sender " + senderId);
            return false;
        }

        // Perform balance updates
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // Record double-entry ledger entries
        ledgerRepository.save(new WalletLedgerEntry(senderId, transactionId, amount.negate(), "TRANSFER_OUT"));
        ledgerRepository.save(new WalletLedgerEntry(receiverId, transactionId, amount, "TRANSFER_IN"));

        System.out.println("✅ Transfer successful: " + amount + " from " + senderId + " to " + receiverId);
        return true;
    }
}
