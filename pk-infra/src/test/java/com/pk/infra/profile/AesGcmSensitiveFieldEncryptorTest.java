package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.pk.core.profile.EncryptedField;
import org.junit.jupiter.api.Test;

class AesGcmSensitiveFieldEncryptorTest {
    private final AesGcmSensitiveFieldEncryptor encryptor =
            new AesGcmSensitiveFieldEncryptor("local-dev-field-encryption-key-32b");

    @Test
    void encryptsAndDecryptsBinaryPayload() {
        byte[] original = new byte[] {0x01, 0x02, (byte) 0xFF, 0x00, 0x7F};

        EncryptedField encrypted = encryptor.encryptBytes(original);

        assertThat(encryptor.decryptBytes(encrypted)).isEqualTo(original);
    }

    @Test
    void encryptsAndDecryptsTextPayload() {
        EncryptedField encrypted = encryptor.encrypt("3301234567890001");

        assertThat(encryptor.decrypt(encrypted)).isEqualTo("3301234567890001");
    }
}
