package com.pk.core.profile;

public record ProfileContactsModuleData(
        long userId,
String moduleStatus,
        String lastRequestId,
        Long externalInteractionId
) {
}
