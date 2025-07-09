package br.com.samarone.wallet.repository;

import br.com.samarone.wallet.model.Wallet;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryWalletRepository implements WalletRepository {

    private final Map<Long, Wallet> wallets = new ConcurrentHashMap<>();

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        return Optional.ofNullable(wallets.get(userId));
    }

    @Override
    public void save(Wallet wallet) {
        wallets.put(wallet.getUserId(), wallet);
    }
}
