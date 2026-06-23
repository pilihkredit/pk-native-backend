package com.pk.infra.auth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.TokenPair;
import com.pk.core.auth.port.TokenIssuer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenIssuer implements TokenIssuer {
    private final AuthProperties authProperties;
    private final byte[] secret;

    public JwtTokenIssuer(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.secret = authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("pk.auth.jwt-secret must be at least 32 characters");
        }
    }

    @Override
    public TokenPair issue(long profileId, String partnerUserId, String mobileNo, long sessionVersion, String deviceId) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plus(authProperties.accessTokenTtl());
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(Long.toString(profileId))
                    .claim("partnerUserId", partnerUserId)
                    .claim("mobile", mobileNo)
                    .claim("sv", sessionVersion)
                    .claim("deviceId", deviceId)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .build();
            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJwt.sign(new MACSigner(secret));
            String refreshTokenId = OtpCodeGenerator.refreshTokenId();
            return TokenPair.of(
                    signedJwt.serialize(),
                    refreshTokenId,
                    authProperties.accessTokenTtl().toSeconds()
            );
        } catch (JOSEException exception) {
            throw new ApiException(ApiCode.INTERNAL_SERVER_ERROR, exception);
        }
    }

    @Override
    public AuthenticatedPrincipal parseAccessToken(String accessToken) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(accessToken);
            if (!signedJwt.verify(new MACVerifier(secret))) {
                throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
            }
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.toInstant().isBefore(Instant.now())) {
                throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
            }
            long profileId = Long.parseLong(Objects.requireNonNull(claims.getSubject()));
            long sessionVersion = claims.getLongClaim("sv");
            String partnerUserId = claims.getStringClaim("partnerUserId");
            String mobileNo = claims.getStringClaim("mobile");
            return new AuthenticatedPrincipal(profileId, partnerUserId, mobileNo, sessionVersion);
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST, exception);
        }
    }
}
