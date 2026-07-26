package com.pk.core.profile;

import java.time.Instant;

public record ProfileIdentityData(
        long userId,
String fullName,
        EncryptedField idNo,
        String idNoHash,
        String moduleStatus,
        String lastRequestId,
        Long externalInteractionId,
        Long profileVersionId,
        String idCardImageEncryptedRef,
        String facePhotoImageEncryptedRef,
        String encryptionKeyRef,
        Instant identityDataRetentionUntil,
        Instant biometricImageRetentionUntil,
        String ocrChannel,
        Long ocrVendorCallLogId
) {
    /** Compatibility constructor used by onboarding / simple upserts without asset fields. */
    public ProfileIdentityData(
            long userId,
            String fullName,
            EncryptedField idNo,
            String idNoHash,
            String moduleStatus,
            String lastRequestId,
            Long externalInteractionId
    ) {
        this(
                userId,
                fullName,
                idNo,
                idNoHash,
                moduleStatus,
                lastRequestId,
                externalInteractionId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
