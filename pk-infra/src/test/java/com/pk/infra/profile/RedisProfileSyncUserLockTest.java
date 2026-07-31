package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisProfileSyncUserLockTest {
    @Test
    void executesOperationWhenLockIsAcquired() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        ProfileSyncLockProperties properties = immediateTimeoutProperties();
        RedisProfileSyncUserLock lock = new RedisProfileSyncUserLock(redisTemplate, properties);

        String result = lock.execute("USER-1", () -> "done");

        assertThat(result).isEqualTo("done");
        verify(valueOperations).setIfAbsent(anyString(), anyString(), any(Duration.class));
        lock.close();
    }

    @Test
    void rejectsOperationWhenLockCannotBeAcquired() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        RedisProfileSyncUserLock lock = new RedisProfileSyncUserLock(redisTemplate, immediateTimeoutProperties());
        AtomicInteger executions = new AtomicInteger();

        assertThatThrownBy(() -> lock.execute("USER-1", () -> executions.incrementAndGet()))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);
        assertThat(executions).hasValue(0);
        lock.close();
    }

    private static ProfileSyncLockProperties immediateTimeoutProperties() {
        ProfileSyncLockProperties properties = new ProfileSyncLockProperties();
        properties.setWaitTimeoutMs(0);
        properties.setLeaseTimeoutMs(60_000);
        properties.setRetryIntervalMs(10);
        return properties;
    }
}
