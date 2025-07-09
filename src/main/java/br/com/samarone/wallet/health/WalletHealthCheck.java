package br.com.samarone.wallet.health;

import br.com.samarone.wallet.repository.WalletRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class WalletHealthCheck implements HealthCheck {

    @Inject
    private WalletRepository walletRepository;

    @Override
    public HealthCheckResponse call() {
        try {
            // A simple check to ensure the repository is accessible
            walletRepository.findByUserId(0L);
            return HealthCheckResponse.named("WalletRepositoryCheck").up().build();
        } catch (Exception e) {
            return HealthCheckResponse.named("WalletRepositoryCheck").down().withData("reason", e.getMessage()).build();
        }
    }
}
