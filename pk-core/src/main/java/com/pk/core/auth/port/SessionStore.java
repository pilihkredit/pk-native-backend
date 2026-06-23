package com.pk.core.auth.port;

import com.pk.core.auth.AuthSession;
import java.time.Duration;
import java.util.Optional;

public interface SessionStore {
    Optional<AuthSession> findByProfileId(long profileId);

    void save(long profileId, AuthSession session, Duration ttl);

    void delete(long profileId);
}
