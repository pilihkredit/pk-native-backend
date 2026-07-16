package com.pk.core.profile;

public record ProfileIdentityData(
        long profileId,
        String mobileNo,
        String fullName,
        EncryptedField idNo,
        String idNoHash,
        String moduleStatus,
        String lastRequestId,
        String lastLenderRequestJson,
        String lastLenderResponseJson,
        Long profileVersionId,
        EncryptedField motherName,
        String idCardImageEncryptedRef,
        String facePhotoImageEncryptedRef,
        String encryptionKeyRef,
        String ocrChannel,
        String ocrResultJson
) {
    /** Compatibility constructor used by onboarding / simple upserts without asset fields. */
    public ProfileIdentityData(
            long profileId,
            String mobileNo,
            String fullName,
            EncryptedField idNo,
            String idNoHash,
            String moduleStatus,
            String lastRequestId,
            String lastLenderRequestJson,
            String lastLenderResponseJson
    ) {
        this(
                profileId,
                mobileNo,
                fullName,
                idNo,
                idNoHash,
                moduleStatus,
                lastRequestId,
                lastLenderRequestJson,
                lastLenderResponseJson,
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
