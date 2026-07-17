package com.pk.infra.auth.repository;

import java.time.Instant;

public record PasswordCredentialRow(
        long profileId,
        String passwordCiphertext,
        byte[] passwordNonce,
        byte[] passwordTag,
        int failedAttempts,
        Instant lockedUntil
) {
}
