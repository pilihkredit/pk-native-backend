package com.pk.infra.profile;

import java.time.Duration;
import java.time.Instant;

public final class FaceVerifyTicketSupport {
    private FaceVerifyTicketSupport() {
    }

    public static long expiresInSeconds(Instant expiresAt, Instant now) {
        if (expiresAt == null) {
            return 0L;
        }
        long seconds = Duration.between(now, expiresAt).getSeconds();
        return Math.max(0L, seconds);
    }
}
