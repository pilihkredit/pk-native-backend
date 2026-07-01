package com.pk.core.profile;

public record ProfileDeviceData(
        long profileId,
        String partnerUserId,
        String deviceNo,
        String systemPlatform,
        String clientAppName,
        String appVersion,
        String packageName,
        String adId,
        String deviceJson,
        String lastRequestId
) {
}
