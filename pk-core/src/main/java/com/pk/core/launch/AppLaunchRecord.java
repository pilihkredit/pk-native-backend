package com.pk.core.launch;

import java.time.Instant;

public record AppLaunchRecord(
        String launchId,
        String deviceNo,
        Long userId,
        String appVersion,
        String platform,
        String appPackage,
        Instant clientStartedAt,
        Instant occurredAt
) {
}
