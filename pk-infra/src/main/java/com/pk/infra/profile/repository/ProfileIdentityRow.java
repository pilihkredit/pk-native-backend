package com.pk.infra.profile.repository;

import java.time.Instant;

public record ProfileIdentityRow(
        long profileId,
        String mobileNo,
        String fullName,
        String idNoCiphertext,
        byte[] idNoNonce,
        byte[] idNoTag,
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
}
