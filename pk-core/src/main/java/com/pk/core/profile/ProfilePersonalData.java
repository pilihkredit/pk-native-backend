package com.pk.core.profile;

public record ProfilePersonalData(
        long userId,
int educationDegree,
        int industry,
        String income,
        EncryptedField motherSurname,
        String userEmail,
        String moduleStatus,
        String lastRequestId,
        Long externalInteractionId
) {
}
