package com.pk.infra.biometric;

import com.pk.core.profile.BiometricImageKind;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.SensitiveFieldEncryptor;

abstract class AbstractEncryptedBiometricImageStore implements BiometricImageStore {
    protected final SensitiveFieldEncryptor sensitiveFieldEncryptor;
    protected final String encryptionKeyRef;
    protected final String environment;
    protected final String pathPrefix;

    protected AbstractEncryptedBiometricImageStore(
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            String encryptionKeyRef,
            String environment,
            String pathPrefix
    ) {
        this.sensitiveFieldEncryptor = sensitiveFieldEncryptor;
        this.encryptionKeyRef = encryptionKeyRef;
        this.environment = environment;
        this.pathPrefix = trimSlashes(pathPrefix);
    }

    @Override
    public String store(long profileId, BiometricImageKind kind, byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("imageBytes must not be empty");
        }
        byte[] encryptedBlob = EncryptedBiometricImageSupport.pack(sensitiveFieldEncryptor.encryptBytes(imageBytes));
        String objectKey = objectKey(profileId, kind);
        writeEncryptedObject(objectKey, encryptedBlob);
        return buildRef(objectKey);
    }

    @Override
    public byte[] load(String encryptedRef) {
        byte[] encryptedBlob = readEncryptedObject(resolveObjectKey(encryptedRef));
        return sensitiveFieldEncryptor.decryptBytes(EncryptedBiometricImageSupport.unpack(encryptedBlob));
    }

    @Override
    public void delete(String encryptedRef) {
        deleteEncryptedObject(resolveObjectKey(encryptedRef));
    }

    @Override
    public String encryptionKeyRef() {
        return encryptionKeyRef;
    }

    protected String objectKey(long profileId, BiometricImageKind kind) {
        return pathPrefix + "/" + environment + "/profile/" + profileId + "/" + kind.objectName() + ".enc";
    }

    protected abstract void writeEncryptedObject(String objectKey, byte[] encryptedBlob);

    protected abstract byte[] readEncryptedObject(String objectKey);

    protected abstract void deleteEncryptedObject(String objectKey);

    protected abstract String scheme();

    protected abstract String buildRef(String objectKey);

    protected abstract String resolveObjectKey(String encryptedRef);

    private static String trimSlashes(String value) {
        if (value == null || value.isBlank()) {
            return "pk-biometric";
        }
        String normalized = value.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isBlank() ? "pk-biometric" : normalized;
    }
}
