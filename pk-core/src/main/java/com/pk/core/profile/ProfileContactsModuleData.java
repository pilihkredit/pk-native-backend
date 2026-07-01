package com.pk.core.profile;

public record ProfileContactsModuleData(
        long profileId,
        String moduleStatus,
        String lastRequestId,
        String lastLenderRequestJson,
        String lastLenderResponseJson
) {
}
