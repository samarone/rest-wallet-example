package br.com.samarone.wallet.service;

import br.com.samarone.wallet.model.Transaction;
import br.com.samarone.wallet.model.Wallet;
import br.com.samarone.wallet.repository.WalletRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class WalletService {

    @Inject
    private WalletRepository walletRepository;

    public Wallet createWallet(Long userId) {
        Wallet wallet = new Wallet(userId);
        walletRepository.save(wallet);
        return wallet;
    }

    public Optional<BigDecimal> getBalance(Long userId) {
        return walletRepository.findByUserId(userId).map(Wallet::getBalance);
    }

    public Optional<BigDecimal> getHistoricalBalance(Long userId, LocalDateTime dateTime) {
        return walletRepository.findByUserId(userId)
                .map(wallet -> wallet.getTransactions().stream()
                        .filter(t -> !t.getTimestamp().isAfter(dateTime))
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public void deposit(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.addTransaction(new Transaction(LocalDateTime.now(), amount));
        walletRepository.save(wallet);
    }

    public void withdraw(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Wallet not found"));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.addTransaction(new Transaction(LocalDateTime.now(), amount.negate()));
        walletRepository.save(wallet);
    }

    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        withdraw(fromUserId, amount);
        deposit(toUserId, amount);
    }
}
