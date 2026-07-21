package com.pk.infra.auth.repository;

public record PasswordCredentialRow(
        long profileId,
        String passwordCiphertext,
        byte[] passwordNonce,
        byte[] passwordTag
) {
}
