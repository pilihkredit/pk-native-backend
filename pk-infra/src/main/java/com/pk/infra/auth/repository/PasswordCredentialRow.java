package com.pk.infra.auth.repository;

public record PasswordCredentialRow(
        long userId,
        String passwordCiphertext,
        byte[] passwordNonce,
        byte[] passwordTag
) {
}
