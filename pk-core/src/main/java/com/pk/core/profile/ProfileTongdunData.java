package com.pk.core.profile;

public record ProfileTongdunData(
        Long id,
        long profileId,
        String mobileNo,
        String sceneType,
        String tongdunKey,
        String moduleStatus,
        String requestId,
        Long externalInteractionId
) {
}
