package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Limits failed public device-switch face verify attempts per mobile + device. */
public class DeviceSwitchLoginFaceAttemptLimiter {
    private static final String KEY_PREFIX = "pk:auth:device-switch-face-fail:";
    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;

    public DeviceSwitchLoginFaceAttemptLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void assertAllowed(String mobileNo, String deviceNo) {
        String key = key(mobileNo, deviceNo);
        String count = redisTemplate.opsForValue().get(key);
        if (count != null && Long.parseLong(count) >= MAX_FAILURES) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
    }

    public void recordFailure(String mobileNo, String deviceNo) {
        String key = key(mobileNo, deviceNo);
        Long next = redisTemplate.opsForValue().increment(key);
        if (next != null && next == 1L) {
            redisTemplate.expire(key, WINDOW);
        }
    }

    public void clearFailures(String mobileNo, String deviceNo) {
        redisTemplate.delete(key(mobileNo, deviceNo));
    }

    private static String key(String mobileNo, String deviceNo) {
        return KEY_PREFIX + mobileNo + ":" + deviceNo;
    }
}
