package br.com.samarone.wallet.repository;

import br.com.samarone.wallet.model.Wallet;

import java.util.Optional;

public interface WalletRepository {

    Optional<Wallet> findByUserId(Long userId);

    void save(Wallet wallet);
}
