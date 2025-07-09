package br.com.samarone.wallet;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

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
}
