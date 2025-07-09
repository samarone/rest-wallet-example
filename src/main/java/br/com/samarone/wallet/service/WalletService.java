package br.com.samarone.wallet.service;

import br.com.samarone.wallet.model.Transaction;
import br.com.samarone.wallet.model.Wallet;
import br.com.samarone.wallet.repository.WalletRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@ApplicationScoped
public class WalletService {

    private static final Logger LOGGER = Logger.getLogger(WalletService.class.getName());

    @Inject
    private WalletRepository walletRepository;

    private final Map<Long, Lock> locks = new ConcurrentHashMap<>();

    public Wallet createWallet(Long userId) {
        LOGGER.info("Creating wallet for user " + userId);
        Wallet wallet = new Wallet(userId);
        walletRepository.save(wallet);
        locks.put(userId, new ReentrantLock());
        LOGGER.info("Wallet created for user " + userId);
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
        LOGGER.info("Depositing " + amount + " to user " + userId);
        Lock lock = locks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
        try {
            Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Wallet not found"));
            wallet.setBalance(wallet.getBalance().add(amount));
            wallet.addTransaction(new Transaction(LocalDateTime.now(), amount));
            walletRepository.save(wallet);
            LOGGER.info("Deposit of " + amount + " to user " + userId + " successful");
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(Long userId, BigDecimal amount) {
        LOGGER.info("Withdrawing " + amount + " from user " + userId);
        Lock lock = locks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
        try {
            Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Wallet not found"));
            if (wallet.getBalance().compareTo(amount) < 0) {
                LOGGER.warning("Insufficient funds for user " + userId);
                throw new RuntimeException("Insufficient funds");
            }
            wallet.setBalance(wallet.getBalance().subtract(amount));
            wallet.addTransaction(new Transaction(LocalDateTime.now(), amount.negate()));
            walletRepository.save(wallet);
            LOGGER.info("Withdrawal of " + amount + " from user " + userId + " successful");
        } finally {
            lock.unlock();
        }
    }

    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        LOGGER.info("Transferring " + amount + " from user " + fromUserId + " to user " + toUserId);
        Lock fromLock = locks.computeIfAbsent(fromUserId, k -> new ReentrantLock());
        Lock toLock = locks.computeIfAbsent(toUserId, k -> new ReentrantLock());

        // to avoid deadlocks, always lock in the same order
        if (fromUserId < toUserId) {
            fromLock.lock();
            toLock.lock();
        } else {
            toLock.lock();
            fromLock.lock();
        }

        try {
            withdraw(fromUserId, amount);
            deposit(toUserId, amount);
            LOGGER.info("Transfer of " + amount + " from user " + fromUserId + " to user " + toUserId + " successful");
        } finally {
            if (fromUserId < toUserId) {
                toLock.unlock();
                fromLock.unlock();
            } else {
                fromLock.unlock();
                toLock.unlock();
            }
        }
    }
}
