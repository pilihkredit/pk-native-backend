package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Limits public device-switch liveness license requests per client IP and per device. */
public class DeviceSwitchLivenessLicenseRateLimiter {
    private static final String IP_KEY_PREFIX = "pk:auth:device-switch-liveness-license:ip:";
    private static final String DEVICE_KEY_PREFIX = "pk:auth:device-switch-liveness-license:device:";

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties.DeviceSwitchLivenessLicenseRateLimit rateLimit;

    public DeviceSwitchLivenessLicenseRateLimiter(
            StringRedisTemplate redisTemplate,
            AuthProperties authProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.rateLimit = authProperties.deviceSwitchLivenessLicenseRateLimit();
    }

    public void assertAllowed(String clientIp, String deviceNo) {
        if (invocationCount(ipKey(clientIp)) >= maxInvocationsPerIp()) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
        if (invocationCount(deviceKey(deviceNo)) >= maxInvocationsPerDevice()) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
    }

    public void recordInvocation(String clientIp, String deviceNo) {
        increment(ipKey(clientIp));
        increment(deviceKey(deviceNo));
    }

    private long invocationCount(String key) {
        String count = redisTemplate.opsForValue().get(key);
        if (count == null || count.isBlank()) {
            return 0L;
        }
        return Long.parseLong(count);
    }

    private void increment(String key) {
        Long next = redisTemplate.opsForValue().increment(key);
        if (next != null && next == 1L) {
            redisTemplate.expire(key, rateLimit.window());
        }
    }

    private int maxInvocationsPerIp() {
        return Math.max(1, rateLimit.maxInvocationsPerIp());
    }

    private int maxInvocationsPerDevice() {
        return Math.max(1, rateLimit.maxInvocationsPerDevice());
    }

    private static String ipKey(String clientIp) {
        return IP_KEY_PREFIX + normalizeClientIp(clientIp);
    }

    private static String deviceKey(String deviceNo) {
        return DEVICE_KEY_PREFIX + AuthDeviceNoNormalizer.requireNonBlank(deviceNo);
    }

    static String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }
        return clientIp.trim();
    }
}
