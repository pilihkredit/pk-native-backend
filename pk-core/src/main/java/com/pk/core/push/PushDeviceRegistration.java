package com.pk.core.push;

public record PushDeviceRegistration(
        Long userId,
        String deviceNo,
        String appVersion,
        String platform,
        String appPackage,
        String fcmToken,
        PushPermissionStatus permissionStatus
) {
}
