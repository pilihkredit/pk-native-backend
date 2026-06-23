package com.pk.core.profile;

public record ProfileContactsModuleData(
        long profileId,
        String moduleStatus,
        String lastRequestId
) {
}
