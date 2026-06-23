package com.pk.core.profile;

public record EncryptedField(String ciphertextBase64, byte[] nonce, byte[] tag) {
}
