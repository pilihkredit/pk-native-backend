package com.pk.core.push;

/** A push_device row resolved as a delivery target. */
public record PushDeviceTarget(long id, Long userId, String fcmToken) {
}
