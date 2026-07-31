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
        String clientAppName,
        DeviceExtendedAttributes extendedAttributes
) {
    public LenderDeviceContext(
            String appName,
            String appVersion,
            String packageName,
            String deviceNo,
            String systemPlatform
    ) {
        this(appName, appVersion, packageName, deviceNo, systemPlatform, null, null, appName, DeviceExtendedAttributes.empty());
    }

    public LenderDeviceContext(
            String appName,
            String appVersion,
            String packageName,
            String deviceNo,
            String systemPlatform,
            String adId,
            Map<String, Object> deviceOtherInfo,
            String clientAppName
    ) {
        this(
                appName,
                appVersion,
                packageName,
                deviceNo,
                systemPlatform,
                adId,
                deviceOtherInfo,
                clientAppName,
                DeviceExtendedAttributes.empty()
        );
    }

    public String resolvedClientAppName() {
        if (clientAppName != null && !clientAppName.isBlank()) {
            return clientAppName.trim();
        }
        return appName;
    }

    public DeviceExtendedAttributes resolvedExtendedAttributes() {
        return extendedAttributes == null ? DeviceExtendedAttributes.empty() : extendedAttributes;
    }
}
