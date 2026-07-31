package com.pk.infra.biometric;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OssBiometricImageStore extends AbstractEncryptedBiometricImageStore {
    private final String bucket;
    private final OSS ossClient;

    public OssBiometricImageStore(
            BiometricStorageProperties properties,
            SensitiveFieldEncryptor sensitiveFieldEncryptor,
            String environment
    ) {
        super(
                sensitiveFieldEncryptor,
                properties.encryptionKeyRef(),
                environment,
                properties.oss().pathPrefix()
        );
        BiometricStorageProperties.Oss oss = properties.oss();
        this.bucket = requireText(oss.bucket(), "pk.biometric-storage.oss.bucket");
        requireText(oss.endpoint(), "pk.biometric-storage.oss.endpoint");
        requireText(oss.accessKeyId(), "pk.biometric-storage.oss.access-key-id");
        requireText(oss.accessKeySecret(), "pk.biometric-storage.oss.access-key-secret");
        this.ossClient = new OSSClientBuilder().build(
                oss.endpoint(),
                oss.accessKeyId(),
                oss.accessKeySecret()
        );
    }

    @Override
    protected void writeEncryptedObject(String objectKey, byte[] encryptedBlob) {
        ossClient.putObject(bucket, objectKey, new java.io.ByteArrayInputStream(encryptedBlob));
    }

    @Override
    protected byte[] readEncryptedObject(String objectKey) {
        try (OSSObject object = ossClient.getObject(bucket, objectKey);
                InputStream inputStream = object.getObjectContent();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read biometric image from OSS", exception);
        }
    }

    @Override
    protected void deleteEncryptedObject(String objectKey) {
        ossClient.deleteObject(bucket, objectKey);
    }

    @Override
    protected String scheme() {
        return "oss";
    }

    @Override
    protected String buildRef(String objectKey) {
        return "oss://" + bucket + "/" + objectKey;
    }

    @Override
    protected String resolveObjectKey(String encryptedRef) {
        String prefix = "oss://" + bucket + "/";
        if (encryptedRef == null || !encryptedRef.startsWith(prefix)) {
            throw new IllegalArgumentException("Unsupported OSS biometric image ref: " + encryptedRef);
        }
        return encryptedRef.substring(prefix.length());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(field + " is required when OSS biometric storage is enabled");
        }
        return value.trim();
    }
}
