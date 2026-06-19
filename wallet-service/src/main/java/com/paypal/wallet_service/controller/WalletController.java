package com.paypal.wallet_service.controller;

import com.paypal.wallet_service.entity.Wallet;
import com.paypal.wallet_service.entity.WalletLedgerEntry;
import com.paypal.wallet_service.repository.WalletLedgerRepository;
import com.paypal.wallet_service.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletRepository walletRepository;
    private final WalletLedgerRepository ledgerRepository;

    public WalletController(WalletRepository walletRepository, WalletLedgerRepository ledgerRepository) {
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long userId) {
        return walletRepository.findById(userId)
                .map(wallet -> ResponseEntity.ok(wallet.getBalance()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/ledger")
    public ResponseEntity<List<WalletLedgerEntry>> getLedger(@PathVariable Long userId) {
        return ResponseEntity.ok(ledgerRepository.findByWalletIdOrderByTimestampDesc(userId));
    }

    @PostMapping("/{userId}/deposit")
    @Transactional
    public ResponseEntity<String> deposit(@PathVariable Long userId, @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount must be positive");
        }

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId).orElse(null);
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        ledgerRepository.save(new WalletLedgerEntry(userId, null, amount, "DEPOSIT"));

        return ResponseEntity.ok("Deposited " + amount + " successfully. New balance: " + wallet.getBalance());
    }
}
