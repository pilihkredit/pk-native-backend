package com.pk.core.auth;

import java.time.Instant;

public record MobileChangeOtpChallenge(
        String otpToken,
        long userId,
        String newMobileNo,
        String deviceNo,
        String faceVerifyToken,
        String otpCode,
        Instant expiresAt,
        String channel
) {
    public MobileChangeOtpChallenge(
            String otpToken,
            long userId,
            String newMobileNo,
            String deviceNo,
            String faceVerifyToken,
            String otpCode,
            Instant expiresAt
    ) {
        this(otpToken, userId, newMobileNo, deviceNo, faceVerifyToken, otpCode, expiresAt, "SMS");
    }

    public MobileChangeOtpChallenge {
        channel = channel == null || channel.isBlank() ? "SMS" : channel;
    }

    public boolean expired(Instant now) {
        return expiresAt == null || !expiresAt.isAfter(now);
    }
}
