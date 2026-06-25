package com.pk.core.auth.port;

import java.time.Instant;
import java.util.Optional;

public interface UserPasswordCredentialRepository {
    boolean isPasswordSet(long profileId);

    Optional<PasswordCredential> findByProfileId(long profileId);

    void insert(long profileId, String passwordHash);

    void recordFailedAttempt(long profileId, int failedAttempts, Instant lockedUntil);

    void resetFailedAttempts(long profileId);

    record PasswordCredential(
            long profileId,
            String passwordHash,
            int failedAttempts,
            Instant lockedUntil
    ) {
    }
}
