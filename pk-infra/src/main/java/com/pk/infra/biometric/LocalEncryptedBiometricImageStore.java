package com.pk.infra.biometric;

import com.pk.core.profile.port.SensitiveFieldEncryptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalEncryptedBiometricImageStore extends AbstractEncryptedBiometricImageStore {
    private final Path baseDir;

    public LocalEncryptedBiometricImageStore(
            BiometricStorageProperties properties,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            String environment
    ) {
        super(
                sensitiveFieldEncryptor,
                properties.encryptionKeyRef(),
                environment,
                properties.local().pathPrefix()
        );
        this.baseDir = Path.of(properties.local().baseDir()).toAbsolutePath().normalize();
    }

    @Override
    protected void writeEncryptedObject(String objectKey, byte[] encryptedBlob) {
        Path target = resolvePath(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, encryptedBlob);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write encrypted biometric image", exception);
        }
    }

    @Override
    protected byte[] readEncryptedObject(String objectKey) {
        try {
            return Files.readAllBytes(resolvePath(objectKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read encrypted biometric image", exception);
        }
    }

    @Override
    protected void deleteEncryptedObject(String objectKey) {
        try {
            Files.deleteIfExists(resolvePath(objectKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to delete encrypted biometric image", exception);
        }
    }

    @Override
    protected String scheme() {
        return "local";
    }

    @Override
    protected String buildRef(String objectKey) {
        return "local://" + objectKey;
    }

    @Override
    protected String resolveObjectKey(String encryptedRef) {
        if (encryptedRef == null || !encryptedRef.startsWith("local://")) {
            throw new IllegalArgumentException("Unsupported local biometric image ref: " + encryptedRef);
        }
        return encryptedRef.substring("local://".length());
    }

    private Path resolvePath(String objectKey) {
        Path resolved = baseDir.resolve(objectKey).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid biometric image object key");
        }
        return resolved;
    }
}
