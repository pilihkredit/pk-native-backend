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
    public String store(String mobileNo, BiometricImageKind kind, byte[] imageBytes) {
        return storeObject(objectKey(mobileNo, kind), imageBytes);
    }

    @Override
    public String storeVersioned(
            String mobileNo,
            BiometricImageKind kind,
            String version,
            byte[] imageBytes
    ) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version must not be blank");
        }
        String safeVersion = version.trim().replaceAll("[^0-9A-Za-z-]", "");
        if (safeVersion.isBlank()) {
            throw new IllegalArgumentException("version must contain a path-safe character");
        }
        String objectKey = pathPrefix + "/" + environment + "/mobile/"
                + sanitizeMobilePathSegment(mobileNo) + "/" + kind.objectName() + "/" + safeVersion + ".enc";
        return storeObject(objectKey, imageBytes);
    }

    private String storeObject(String objectKey, byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("imageBytes must not be empty");
        }
        byte[] encryptedBlob = EncryptedBiometricImageSupport.pack(sensitiveFieldEncryptor.encryptBytes(imageBytes));
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

    protected String objectKey(String mobileNo, BiometricImageKind kind) {
        return pathPrefix + "/" + environment + "/mobile/" + sanitizeMobilePathSegment(mobileNo)
                + "/" + kind.objectName() + ".enc";
    }

    /** Keep path-safe digits/letters only; reject empty after sanitize. */
    protected static String sanitizeMobilePathSegment(String mobileNo) {
        if (mobileNo == null || mobileNo.isBlank()) {
            throw new IllegalArgumentException("mobileNo is required for biometric image path");
        }
        String sanitized = mobileNo.trim().replaceAll("[^0-9A-Za-z]", "");
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("mobileNo is required for biometric image path");
        }
        return sanitized;
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
