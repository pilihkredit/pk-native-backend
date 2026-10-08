package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Limits failed public device-switch face verify attempts per mobile + device. */
public class DeviceSwitchLoginFaceAttemptLimiter {
    private static final String KEY_PREFIX = "pk:auth:device-switch-face-fail:";

    private final StringRedisTemplate redisTemplate;
    private final DeviceSwitchSecurityConfigLoader configLoader;

    public DeviceSwitchLoginFaceAttemptLimiter(
            StringRedisTemplate redisTemplate,
            DeviceSwitchSecurityConfigLoader configLoader
    ) {
        this.redisTemplate = redisTemplate;
        this.configLoader = configLoader;
    }

    public void assertAllowed(String mobileNo, String deviceNo) {
        DeviceSwitchSecurityConfigLoader.Settings settings = configLoader.loadSettings();
        String key = key(mobileNo, deviceNo);
        String count = redisTemplate.opsForValue().get(key);
        if (count != null && Long.parseLong(count) >= settings.faceVerifyMaxFailures()) {
            throw new ApiException(ApiCode.TOO_MANY_REQUESTS);
        }
    }

    public void recordFailure(String mobileNo, String deviceNo) {
        DeviceSwitchSecurityConfigLoader.Settings settings = configLoader.loadSettings();
        String key = key(mobileNo, deviceNo);
        Long next = redisTemplate.opsForValue().increment(key);
        if (next != null && next == 1L) {
            redisTemplate.expire(key, settings.faceVerifyFailWindow());
        }
    }

    public void clearFailures(String mobileNo, String deviceNo) {
        redisTemplate.delete(key(mobileNo, deviceNo));
    }

    private static String key(String mobileNo, String deviceNo) {
        return KEY_PREFIX + mobileNo + ":" + deviceNo;
    }
}
