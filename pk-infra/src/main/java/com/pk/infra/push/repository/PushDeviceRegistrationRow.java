package com.pk.infra.push.repository;

public record PushDeviceRegistrationRow(
        Long userId,
        String deviceNo,
        String appVersion,
        String platform,
        String appPackage,
        String fcmToken,
        String permissionStatus
) {
}
