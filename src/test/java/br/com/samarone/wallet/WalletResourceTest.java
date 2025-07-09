package br.com.samarone.wallet;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@HelidonTest
class WalletResourceTest {

    @Inject
    private WebTarget webTarget;

    @Test
    void testCreateWallet() {
        Response response = webTarget.path("/wallets/1").request().post(null);
        assertThat(response.getStatus(), is(201));
    }

    @Test
    void testConcurrentWithdrawals() throws InterruptedException {
        // Create a wallet for user 2
        webTarget.path("/wallets/2").request().post(null);

        // Deposit 1000 into the wallet
        webTarget.path("/wallets/2/deposit").request().post(Entity.entity(new BigDecimal("1000"), MediaType.APPLICATION_JSON));

        // Concurrently withdraw 100 times, 10 from each withdrawal
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                webTarget.path("/wallets/2/withdraw").request().post(Entity.entity(new BigDecimal("10"), MediaType.APPLICATION_JSON));
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // Check the final balance
        Response response = webTarget.path("/wallets/2/balance").request().get();
        BigDecimal balance = response.readEntity(BigDecimal.class);
        assertThat(balance.compareTo(BigDecimal.ZERO), is(0));
    }
}
