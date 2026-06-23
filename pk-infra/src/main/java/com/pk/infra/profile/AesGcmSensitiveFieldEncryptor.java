package com.pk.infra.profile;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesGcmSensitiveFieldEncryptor implements SensitiveFieldEncryptor {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int NONCE_BYTES = 12;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public AesGcmSensitiveFieldEncryptor(String secret) {
        this.secretKey = new SecretKeySpec(deriveKey(secret), "AES");
        this.secureRandom = new SecureRandom();
    }

    @Override
    public EncryptedField encrypt(String plaintext) {
        try {
            byte[] nonce = new byte[NONCE_BYTES];
            secureRandom.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            int tagOffset = ciphertext.length - 16;
            byte[] body = new byte[tagOffset];
            byte[] tag = new byte[16];
            System.arraycopy(ciphertext, 0, body, 0, tagOffset);
            System.arraycopy(ciphertext, tagOffset, tag, 0, 16);
            return new EncryptedField(Base64.getEncoder().encodeToString(body), nonce, tag);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to encrypt sensitive field", exception);
        }
    }

    private static byte[] deriveKey(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to derive encryption key", exception);
        }
    }
}
