package com.pk.core.profile;

public record ProfilePersonalData(
        long profileId,
        int educationDegree,
        int industry,
        String income,
        EncryptedField motherSurname,
        String userEmail,
        String moduleStatus,
        String lastRequestId,
        String lastLenderRequestJson,
        String lastLenderResponseJson
) {
}
