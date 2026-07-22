package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.TokenPair;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
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

    @Test
    void parseAccessTokenRejectsExpiredTokenWithoutClientDetail() throws Exception {
        String secret = "local-dev-secret-change-in-prod-min-32-chars";
        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setJwtSecret(secret);
        JwtTokenIssuer issuer = new JwtTokenIssuer(properties);

        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("42")
                .claim("partnerUserId", "U10001")
                .claim("mobile", "81234567890")
                .claim("sv", 1L)
                .claim("deviceId", "device-1")
                .issueTime(Date.from(now.minusSeconds(120)))
                .expirationTime(Date.from(now.minusSeconds(60)))
                .build();
        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signedJwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));

        assertThatThrownBy(() -> issuer.parseAccessToken(signedJwt.serialize()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiException = (ApiException) ex;
                    assertThat(apiException.apiCode()).isEqualTo(ApiCode.UNAUTHORIZED_REQUEST);
                    assertThat(apiException.detail()).isNull();
                });
    }
}
