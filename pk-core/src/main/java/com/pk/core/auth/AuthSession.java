package com.pk.core.auth;

import java.time.Instant;

public record AuthSession(
        long userId,
        long sessionVersion,
        String deviceId,
        String loginChannel,
        Instant issuedAt
) {
}
