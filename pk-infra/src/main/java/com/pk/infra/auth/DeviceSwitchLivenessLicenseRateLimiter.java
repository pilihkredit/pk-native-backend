package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

/**
 * Sliding-window invocation limits for public device-switch liveness license; exceeding the window
 * triggers a separate block period per IP and per device.
 */
public class DeviceSwitchLivenessLicenseRateLimiter {
    public static final String CLIENT_MESSAGE =
            "Too many verification attempts. Please try again later.";

    private static final String IP_EVENTS_PREFIX = "pk:auth:device-switch-liveness-license:ip:";
    private static final String IP_BLOCK_SUFFIX = ":blocked-until";
    private static final String DEVICE_EVENTS_PREFIX = "pk:auth:device-switch-liveness-license:device:";
    private static final String DEVICE_BLOCK_SUFFIX = ":blocked-until";
    private static final String EVENTS_SUFFIX = ":events";

    private final StringRedisTemplate redisTemplate;
    private final DeviceSwitchSecurityConfigLoader configLoader;

    public DeviceSwitchLivenessLicenseRateLimiter(
            StringRedisTemplate redisTemplate,
            DeviceSwitchSecurityConfigLoader configLoader
    ) {
        this.redisTemplate = redisTemplate;
        this.configLoader = configLoader;
    }

    /**
     * Validates both dimensions and records one successful invocation when allowed.
     */
    public void checkAndRecord(String clientIp, String deviceNo) {
        DeviceSwitchSecurityConfigLoader.Settings settings = configLoader.loadSettings();
        String normalizedIp = normalizeClientIp(clientIp);
        String normalizedDevice = AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
        long nowMillis = Instant.now().toEpochMilli();
        assertWindowAllowed(
                ipEventsKey(normalizedIp),
                ipBlockKey(normalizedIp),
                settings.livenessLicenseMaxPerIp(),
                settings.livenessLicenseWindow(),
                settings.livenessLicenseBlock(),
                nowMillis
        );
        assertWindowAllowed(
                deviceEventsKey(normalizedDevice),
                deviceBlockKey(normalizedDevice),
                settings.livenessLicenseMaxPerDevice(),
                settings.livenessLicenseWindow(),
                settings.livenessLicenseBlock(),
                nowMillis
        );
        recordEvent(ipEventsKey(normalizedIp), nowMillis, settings.livenessLicenseWindow());
        recordEvent(deviceEventsKey(normalizedDevice), nowMillis, settings.livenessLicenseWindow());
    }

    private void assertWindowAllowed(
            String eventsKey,
            String blockKey,
            int maxInvocations,
            Duration window,
            Duration block,
            long nowMillis
    ) {
        assertNotBlocked(blockKey);
        long windowStartMillis = nowMillis - window.toMillis();
        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();
        zSet.removeRangeByScore(eventsKey, 0, windowStartMillis);
        Long count = zSet.size(eventsKey);
        long current = count == null ? 0L : count;
        if (current >= maxInvocations) {
            triggerBlock(blockKey, eventsKey, nowMillis, block);
            throw rateLimitException();
        }
    }

    private void recordEvent(String eventsKey, long nowMillis, Duration window) {
        redisTemplate.opsForZSet().add(eventsKey, UUID.randomUUID().toString(), nowMillis);
        redisTemplate.expire(eventsKey, window.plus(Duration.ofMinutes(1)));
    }

    private void assertNotBlocked(String blockKey) {
        String blockedUntil = redisTemplate.opsForValue().get(blockKey);
        if (blockedUntil == null || blockedUntil.isBlank()) {
            return;
        }
        long untilMillis = Long.parseLong(blockedUntil.trim());
        long nowMillis = Instant.now().toEpochMilli();
        if (nowMillis < untilMillis) {
            throw rateLimitException();
        }
        redisTemplate.delete(blockKey);
    }

    private void triggerBlock(String blockKey, String eventsKey, long nowMillis, Duration block) {
        long blockedUntilMillis = nowMillis + block.toMillis();
        redisTemplate.opsForValue().set(blockKey, Long.toString(blockedUntilMillis), block);
        redisTemplate.delete(eventsKey);
    }

    private static ApiException rateLimitException() {
        return new ApiException(ApiCode.TOO_MANY_REQUESTS, CLIENT_MESSAGE);
    }

    private static String ipEventsKey(String clientIp) {
        return IP_EVENTS_PREFIX + clientIp + EVENTS_SUFFIX;
    }

    private static String ipBlockKey(String clientIp) {
        return IP_EVENTS_PREFIX + clientIp + IP_BLOCK_SUFFIX;
    }

    private static String deviceEventsKey(String deviceNo) {
        return DEVICE_EVENTS_PREFIX + deviceNo + EVENTS_SUFFIX;
    }

    private static String deviceBlockKey(String deviceNo) {
        return DEVICE_EVENTS_PREFIX + deviceNo + DEVICE_BLOCK_SUFFIX;
    }

    static String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }
        return clientIp.trim();
    }
}
