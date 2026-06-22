package com.pk.app.auth;

import com.pk.app.config.AuthProperties;
import com.pk.app.web.AuthenticatedUser;
import com.pk.infra.user.UserProfileRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final AuthProperties authProperties;
    private final UserProfileRepository userProfileRepository;
    private final SecretKey secretKey;

    public JwtTokenService(AuthProperties authProperties, UserProfileRepository userProfileRepository) {
        this.authProperties = authProperties;
        this.userProfileRepository = userProfileRepository;
        this.secretKey = Keys.hmacShaKeyFor(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public IssuedToken issueToken(long profileId, String partnerUserId, String mobileNo) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(authProperties.accessTokenTtlSeconds());
        String token = Jwts.builder()
                .subject(partnerUserId)
                .claim("pid", profileId)
                .claim("mobile", mobileNo)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new IssuedToken(token, authProperties.accessTokenTtlSeconds());
    }

    public AuthenticatedUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String partnerUserId = claims.getSubject();
            return userProfileRepository.findActiveByPartnerUserId(partnerUserId)
                    .map(profile -> new AuthenticatedUser(
                            profile.id(),
                            profile.partnerUserId(),
                            profile.mobileNo(),
                            profile.kycStatus()
                    ))
                    .orElseThrow(() -> new com.pk.core.error.AppBusinessException(
                            com.pk.core.error.AppErrorCodes.UNAUTHORIZED));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new com.pk.core.error.AppBusinessException(com.pk.core.error.AppErrorCodes.UNAUTHORIZED);
        }
    }

    public record IssuedToken(String accessToken, long expiresInSeconds) {
    }
}
