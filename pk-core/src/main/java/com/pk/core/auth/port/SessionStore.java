package com.pk.core.auth.port;

import com.pk.core.auth.AuthSession;
import java.time.Duration;
import java.util.Optional;

public interface SessionStore {
    Optional<AuthSession> findByUserId(long userId);

    void save(long userId, AuthSession session, Duration ttl);

    void delete(long userId);
}
