package com.pk.infra.biometric;

import com.pk.core.profile.EncryptedField;
import java.util.Arrays;
import java.util.Base64;

final class EncryptedBiometricImageSupport {
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BYTES = 16;

    private EncryptedBiometricImageSupport() {
    }

    static byte[] pack(EncryptedField encryptedField) {
        byte[] body = Base64.getDecoder().decode(encryptedField.ciphertextBase64());
        byte[] packed = new byte[NONCE_BYTES + TAG_BYTES + body.length];
        System.arraycopy(encryptedField.nonce(), 0, packed, 0, NONCE_BYTES);
        System.arraycopy(encryptedField.tag(), 0, packed, NONCE_BYTES, TAG_BYTES);
        System.arraycopy(body, 0, packed, NONCE_BYTES + TAG_BYTES, body.length);
        return packed;
    }

    static EncryptedField unpack(byte[] packed) {
        if (packed.length < NONCE_BYTES + TAG_BYTES) {
            throw new IllegalStateException("Encrypted biometric image blob is too short");
        }
        byte[] nonce = Arrays.copyOfRange(packed, 0, NONCE_BYTES);
        byte[] tag = Arrays.copyOfRange(packed, NONCE_BYTES, NONCE_BYTES + TAG_BYTES);
        byte[] body = Arrays.copyOfRange(packed, NONCE_BYTES + TAG_BYTES, packed.length);
        return new EncryptedField(Base64.getEncoder().encodeToString(body), nonce, tag);
    }
}
