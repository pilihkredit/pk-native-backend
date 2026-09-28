package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeviceSwitchLivenessLicenseRateLimiterTest {
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private DeviceSwitchLivenessLicenseRateLimiter limiter;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        AuthProperties authProperties = new AuthProperties();
        limiter = new DeviceSwitchLivenessLicenseRateLimiter(redisTemplate, authProperties);
    }

    @Test
    void rejectsWhenIpLimitReached() {
        when(valueOperations.get("pk:auth:device-switch-liveness-license:ip:1.2.3.4")).thenReturn("5");
        when(valueOperations.get("pk:auth:device-switch-liveness-license:device:device-1")).thenReturn("0");

        assertThatThrownBy(() -> limiter.assertAllowed("1.2.3.4", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).apiCode())
                .isEqualTo(ApiCode.TOO_MANY_REQUESTS);
    }

    @Test
    void rejectsWhenDeviceLimitReached() {
        when(valueOperations.get("pk:auth:device-switch-liveness-license:ip:1.2.3.4")).thenReturn("1");
        when(valueOperations.get("pk:auth:device-switch-liveness-license:device:device-1")).thenReturn("5");

        assertThatThrownBy(() -> limiter.assertAllowed("1.2.3.4", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).apiCode())
                .isEqualTo(ApiCode.TOO_MANY_REQUESTS);
    }

    @Test
    void allowsWhenBelowLimit() {
        when(valueOperations.get(anyString())).thenReturn(null);

        assertThatCode(() -> limiter.assertAllowed("1.2.3.4", "device-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void recordInvocationIncrementsBothKeys() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        limiter.recordInvocation("1.2.3.4", "device-1");

        org.mockito.Mockito.verify(valueOperations).increment(
                eq("pk:auth:device-switch-liveness-license:ip:1.2.3.4"));
        org.mockito.Mockito.verify(valueOperations).increment(
                eq("pk:auth:device-switch-liveness-license:device:device-1"));
        org.mockito.Mockito.verify(redisTemplate, org.mockito.Mockito.times(2))
                .expire(anyString(), eq(java.time.Duration.ofMinutes(5)));
    }

    @Test
    void usesConfiguredLimits() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.deviceSwitchLivenessLicenseRateLimit().setMaxInvocationsPerIp(3);
        authProperties.deviceSwitchLivenessLicenseRateLimit().setMaxInvocationsPerDevice(4);
        limiter = new DeviceSwitchLivenessLicenseRateLimiter(redisTemplate, authProperties);

        when(valueOperations.get("pk:auth:device-switch-liveness-license:ip:1.2.3.4")).thenReturn("3");
        when(valueOperations.get("pk:auth:device-switch-liveness-license:device:device-1")).thenReturn("0");

        assertThatThrownBy(() -> limiter.assertAllowed("1.2.3.4", "device-1"))
                .isInstanceOf(ApiException.class);
    }
}
