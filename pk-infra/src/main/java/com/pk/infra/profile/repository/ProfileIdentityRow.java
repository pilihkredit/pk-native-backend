package com.pk.infra.profile.repository;

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
        String lastLenderRequestJson,
        String lastLenderResponseJson,
        Long profileVersionId,
        String motherNameCiphertext,
        byte[] motherNameNonce,
        byte[] motherNameTag,
        String idCardImageEncryptedRef,
        String facePhotoImageEncryptedRef,
        String encryptionKeyRef,
        String ocrChannel,
        String ocrResultJson
) {
}
