package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.TokenPair;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtTokenIssuerTest {
    @Test
    void issuesAndParsesAccessToken() {
        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");

        JwtTokenIssuer issuer = new JwtTokenIssuer(properties);
        TokenPair tokenPair = issuer.issue(42L, "U10001", "81234567890", 3L, "device-1");

        assertThat(tokenPair.accessToken()).isNotBlank();
        assertThat(tokenPair.refreshToken()).startsWith("rt_");
        assertThat(tokenPair.accessTokenExpiresInSeconds()).isEqualTo(900L);

        AuthenticatedPrincipal principal = issuer.parseAccessToken(tokenPair.accessToken());
        assertThat(principal.profileId()).isEqualTo(42L);
        assertThat(principal.partnerUserId()).isEqualTo("U10001");
        assertThat(principal.sessionVersion()).isEqualTo(3L);
    }
}
