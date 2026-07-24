package com.pk.core.profile;

public record ProfileContactsModuleData(
        long profileId,
        String mobileNo,
        String moduleStatus,
        String lastRequestId,
        Long externalInteractionId
) {
}
