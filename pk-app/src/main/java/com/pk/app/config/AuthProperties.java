package com.pk.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.auth")
public record AuthProperties(
        String jwtSecret,
        long accessTokenTtlSeconds,
        int otpExpireSeconds,
        int otpResendSeconds,
        String otpFixedCode
) {
    public AuthProperties {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("pk.auth.jwt-secret is required");
        }
        if (accessTokenTtlSeconds <= 0) {
            accessTokenTtlSeconds = 604_800L;
        }
        if (otpExpireSeconds <= 0) {
            otpExpireSeconds = 300;
        }
        if (otpResendSeconds <= 0) {
            otpResendSeconds = 60;
        }
    }
}
