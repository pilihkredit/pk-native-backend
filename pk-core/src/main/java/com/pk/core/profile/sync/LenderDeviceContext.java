package com.pk.core.profile.sync;

import java.util.Map;

public record LenderDeviceContext(
        String appName,
        String appVersion,
        String packageName,
        String deviceNo,
        String systemPlatform,
        String adId,
        Map<String, Object> deviceOtherInfo,
        String clientAppName
) {
    public LenderDeviceContext(
            String appName,
            String appVersion,
            String packageName,
            String deviceNo,
            String systemPlatform
    ) {
        this(appName, appVersion, packageName, deviceNo, systemPlatform, null, null, appName);
    }

    public String resolvedClientAppName() {
        if (clientAppName != null && !clientAppName.isBlank()) {
            return clientAppName.trim();
        }
        return appName;
    }
}
