package com.pk.infra.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.infra.auth.AuthProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CallbackTokenIssuerTest {
    private CallbackTokenIssuer issuer;

    @BeforeEach
    void setUp() {
        CallbackProperties properties = new CallbackProperties();
        CallbackProperties.OAuth oauth = new CallbackProperties.OAuth();
        oauth.setAccessTokenTtl(Duration.ofHours(2));
        properties.setOauth(oauth);
        AuthProperties authProperties = new AuthProperties();
        authProperties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");
        issuer = new CallbackTokenIssuer(properties, authProperties);
    }

    @Test
    void issuesAndValidatesCallbackToken() {
        CallbackTokenIssuer.CallbackAccessToken token = issuer.issueToken();

        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.expiresInSeconds()).isEqualTo(Duration.ofHours(2).toSeconds());
        issuer.validateToken(token.accessToken());
    }

    @Test
    void rejectsInvalidToken() {
        assertThatThrownBy(() -> issuer.validateToken("invalid-token"))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.UNAUTHORIZED_REQUEST);
    }
}
