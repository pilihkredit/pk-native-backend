package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

class DeviceSwitchLivenessLicenseRateLimiterTest {
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private ZSetOperations<String, String> zSetOperations;
    private DeviceSwitchLivenessLicenseRateLimiter limiter;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        zSetOperations = mock(ZSetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        AuthProperties authProperties = new AuthProperties();
        limiter = new DeviceSwitchLivenessLicenseRateLimiter(redisTemplate, authProperties);
    }

    @Test
    void rejectsWhenDeviceSlidingWindowFull() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(zSetOperations.size("pk:auth:device-switch-liveness-license:device:device-1:events"))
                .thenReturn(5L);
        when(zSetOperations.size("pk:auth:device-switch-liveness-license:ip:1.2.3.4:events"))
                .thenReturn(0L);

        assertThatThrownBy(() -> limiter.checkAndRecord("1.2.3.4", "device-1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.apiCode()).isEqualTo(ApiCode.TOO_MANY_REQUESTS);
                    assertThat(api.detail()).isEqualTo(DeviceSwitchLivenessLicenseRateLimiter.CLIENT_MESSAGE);
                });

        verify(valueOperations).set(
                eq("pk:auth:device-switch-liveness-license:device:device-1:blocked-until"),
                anyString(),
                eq(Duration.ofMinutes(15))
        );
        verify(redisTemplate).delete("pk:auth:device-switch-liveness-license:device:device-1:events");
    }

    @Test
    void rejectsWhenDimensionStillBlocked() {
        long future = Instant.now().plus(Duration.ofMinutes(10)).toEpochMilli();
        when(valueOperations.get("pk:auth:device-switch-liveness-license:ip:1.2.3.4:blocked-until"))
                .thenReturn(Long.toString(future));

        assertThatThrownBy(() -> limiter.checkAndRecord("1.2.3.4", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).detail())
                .isEqualTo(DeviceSwitchLivenessLicenseRateLimiter.CLIENT_MESSAGE);

        verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
    }

    @Test
    void recordsWhenBelowLimit() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(zSetOperations.size(anyString())).thenReturn(0L);

        assertThatCode(() -> limiter.checkAndRecord("1.2.3.4", "device-1"))
                .doesNotThrowAnyException();

        verify(zSetOperations).add(
                eq("pk:auth:device-switch-liveness-license:device:device-1:events"),
                anyString(),
                anyDouble()
        );
        verify(zSetOperations).add(
                eq("pk:auth:device-switch-liveness-license:ip:1.2.3.4:events"),
                anyString(),
                anyDouble()
        );
    }

    @Test
    void clearsExpiredBlockBeforeCounting() {
        long past = Instant.now().minus(Duration.ofMinutes(1)).toEpochMilli();
        when(valueOperations.get("pk:auth:device-switch-liveness-license:ip:1.2.3.4:blocked-until"))
                .thenReturn(Long.toString(past));
        when(valueOperations.get("pk:auth:device-switch-liveness-license:device:device-1:blocked-until"))
                .thenReturn(null);
        when(zSetOperations.size(anyString())).thenReturn(0L);

        assertThatCode(() -> limiter.checkAndRecord("1.2.3.4", "device-1"))
                .doesNotThrowAnyException();

        verify(redisTemplate).delete("pk:auth:device-switch-liveness-license:ip:1.2.3.4:blocked-until");
    }
}
