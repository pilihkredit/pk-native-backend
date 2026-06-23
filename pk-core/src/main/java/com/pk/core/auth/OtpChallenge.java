package com.pk.core.auth;

import java.time.Instant;

public record OtpChallenge(
        String mobileNo,
        String deviceNo,
        String otpCode,
        Instant expiresAt
) {
    public boolean expired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
