package com.pk.core.auth;

import java.time.Instant;

public record MobileChangeOtpChallenge(
        String otpToken,
        long userId,
        String newMobileNo,
        String deviceNo,
        String faceVerifyToken,
        String otpCode,
        Instant expiresAt
) {
    public boolean expired(Instant now) {
        return expiresAt == null || !expiresAt.isAfter(now);
    }
}
