package com.pk.infra.callback;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.infra.auth.AuthProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class CallbackTokenIssuer {
    private static final String TOKEN_TYPE = "callback";

    private final CallbackProperties callbackProperties;
    private final byte[] secret;

    public CallbackTokenIssuer(CallbackProperties callbackProperties, AuthProperties authProperties) {
        this.callbackProperties = callbackProperties;
        this.secret = authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("pk.auth.jwt-secret must be at least 32 characters");
        }
    }

    public CallbackAccessToken issueToken() {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plus(callbackProperties.oauth().accessTokenTtl());
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject("callback")
                    .claim("typ", TOKEN_TYPE)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .build();
            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJwt.sign(new MACSigner(secret));
            return new CallbackAccessToken(
                    signedJwt.serialize(),
                    callbackProperties.oauth().accessTokenTtl().toSeconds()
            );
        } catch (JOSEException exception) {
            throw new ApiException(ApiCode.INTERNAL_SERVER_ERROR, exception);
        }
    }

    public void validateToken(String accessToken) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(accessToken);
            if (!signedJwt.verify(new MACVerifier(secret))) {
                throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
            }
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            if (!TOKEN_TYPE.equals(claims.getStringClaim("typ"))) {
                throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
            }
            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.toInstant().isBefore(Instant.now())) {
                throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST, exception);
        }
    }

    public record CallbackAccessToken(String accessToken, long expiresInSeconds) {
    }
}
