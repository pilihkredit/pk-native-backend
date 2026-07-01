package com.pk.core.profile;

public record ProfileIdentityData(
        long profileId,
        String fullName,
        EncryptedField idNo,
        String idNoHash,
        String moduleStatus,
        String lastRequestId,
        String lastLenderRequestJson,
        String lastLenderResponseJson
) {
}
