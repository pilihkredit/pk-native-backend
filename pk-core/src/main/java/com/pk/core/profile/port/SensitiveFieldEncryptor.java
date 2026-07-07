package com.pk.core.profile.port;

import com.pk.core.profile.EncryptedField;

public interface SensitiveFieldEncryptor {
    EncryptedField encrypt(String plaintext);

    String decrypt(EncryptedField encryptedField);

    EncryptedField encryptBytes(byte[] plaintext);

    byte[] decryptBytes(EncryptedField encryptedField);
}
