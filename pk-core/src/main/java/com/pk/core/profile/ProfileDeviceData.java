package com.pk.core.profile;

import java.util.Map;

public record ProfileDeviceData(
        long profileId,
        String deviceNo,
        String systemPlatform,
        String clientAppName,
        String appVersion,
        String packageName,
        String adId,
        Map<String, Object> deviceOtherInfo,
        String lastRequestId
) {
}
