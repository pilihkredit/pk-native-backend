package com.pk.core.auth.port;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {
    record RefreshTokenRecord(long userId, long sessionVersion, String deviceId) {
    }

    void save(String refreshTokenId, RefreshTokenRecord record, Duration ttl);

    Optional<RefreshTokenRecord> find(String refreshTokenId);

    void delete(String refreshTokenId);

    void deleteAllForProfile(long userId);
}
