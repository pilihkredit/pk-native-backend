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
    private final AuthProperties.DeviceSwitchLivenessLicenseRateLimit rateLimit;

    public DeviceSwitchLivenessLicenseRateLimiter(
            StringRedisTemplate redisTemplate,
            AuthProperties authProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.rateLimit = authProperties.deviceSwitchLivenessLicenseRateLimit();
    }

    /**
     * Validates both dimensions and records one successful invocation when allowed.
     */
    public void checkAndRecord(String clientIp, String deviceNo) {
        String normalizedIp = normalizeClientIp(clientIp);
        String normalizedDevice = AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
        long nowMillis = Instant.now().toEpochMilli();
        assertWindowAllowed(
                ipEventsKey(normalizedIp),
                ipBlockKey(normalizedIp),
                maxInvocationsPerIp(),
                nowMillis
        );
        assertWindowAllowed(
                deviceEventsKey(normalizedDevice),
                deviceBlockKey(normalizedDevice),
                maxInvocationsPerDevice(),
                nowMillis
        );
        recordEvent(ipEventsKey(normalizedIp), nowMillis);
        recordEvent(deviceEventsKey(normalizedDevice), nowMillis);
    }

    private void assertWindowAllowed(String eventsKey, String blockKey, int maxInvocations, long nowMillis) {
        assertNotBlocked(blockKey);
        long windowStartMillis = nowMillis - windowMillis();
        ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();
        zSet.removeRangeByScore(eventsKey, 0, windowStartMillis);
        Long count = zSet.size(eventsKey);
        long current = count == null ? 0L : count;
        if (current >= maxInvocations) {
            triggerBlock(blockKey, eventsKey, nowMillis);
            throw rateLimitException();
        }
    }

    private void recordEvent(String eventsKey, long nowMillis) {
        redisTemplate.opsForZSet().add(eventsKey, UUID.randomUUID().toString(), nowMillis);
        redisTemplate.expire(eventsKey, windowDuration().plus(Duration.ofMinutes(1)));
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

    private void triggerBlock(String blockKey, String eventsKey, long nowMillis) {
        long blockedUntilMillis = nowMillis + blockMillis();
        Duration blockDuration = blockDuration();
        redisTemplate.opsForValue().set(blockKey, Long.toString(blockedUntilMillis), blockDuration);
        redisTemplate.delete(eventsKey);
    }

    private static ApiException rateLimitException() {
        return new ApiException(ApiCode.TOO_MANY_REQUESTS, CLIENT_MESSAGE);
    }

    private long windowMillis() {
        return windowDuration().toMillis();
    }

    private long blockMillis() {
        return blockDuration().toMillis();
    }

    private Duration windowDuration() {
        return rateLimit.window();
    }

    private Duration blockDuration() {
        return rateLimit.block();
    }

    private int maxInvocationsPerIp() {
        return Math.max(1, rateLimit.maxInvocationsPerIp());
    }

    private int maxInvocationsPerDevice() {
        return Math.max(1, rateLimit.maxInvocationsPerDevice());
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
