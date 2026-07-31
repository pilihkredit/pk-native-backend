package com.pk.infra.profile;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RedisProfileSyncUserLock implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(RedisProfileSyncUserLock.class);
    private static final String KEY_PREFIX = "pk:profile-sync:";
    private static final DefaultRedisScript<Long> RENEW_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end",
            Long.class
    );
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final ProfileSyncLockProperties properties;
    private final ScheduledExecutorService renewalExecutor;

    public RedisProfileSyncUserLock(
            StringRedisTemplate redisTemplate,
            ProfileSyncLockProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.renewalExecutor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory());
    }

    public <T> T execute(String partnerUserId, Supplier<T> operation) {
        String key = lockKey(partnerUserId);
        String token = UUID.randomUUID().toString();
        acquire(key, token);
        ScheduledFuture<?> renewal = scheduleRenewal(key, token);
        try {
            return operation.get();
        } finally {
            renewal.cancel(false);
            release(key, token);
        }
    }

    private void acquire(String key, String token) {
        validateProperties();
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(properties.waitTimeoutMs());
        do {
            try {
                Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                        key,
                        token,
                        Duration.ofMillis(properties.leaseTimeoutMs())
                );
                if (Boolean.TRUE.equals(acquired)) {
                    return;
                }
            } catch (RuntimeException exception) {
                throw serviceUnavailable(exception);
            }
            if (System.nanoTime() >= deadline) {
                throw serviceUnavailable(null);
            }
            sleepBeforeRetry(deadline);
        } while (true);
    }

    private ScheduledFuture<?> scheduleRenewal(String key, String token) {
        long intervalMs = Math.max(1, properties.leaseTimeoutMs() / 3);
        return renewalExecutor.scheduleAtFixedRate(
                () -> renew(key, token),
                intervalMs,
                intervalMs,
                TimeUnit.MILLISECONDS
        );
    }

    private void renew(String key, String token) {
        try {
            Long renewed = redisTemplate.execute(
                    RENEW_SCRIPT,
                    List.of(key),
                    token,
                    Long.toString(properties.leaseTimeoutMs())
            );
            if (!Long.valueOf(1L).equals(renewed)) {
                log.warn("Profile sync lock renewal lost ownership key={}", key);
            }
        } catch (RuntimeException exception) {
            log.warn("Profile sync lock renewal failed key={}", key, exception);
        }
    }

    private void release(String key, String token) {
        try {
            redisTemplate.execute(RELEASE_SCRIPT, List.of(key), token);
        } catch (RuntimeException exception) {
            log.warn("Profile sync lock release failed key={}", key, exception);
        }
    }

    private void sleepBeforeRetry(long deadline) {
        long remainingMs = Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()));
        long sleepMs = Math.min(properties.retryIntervalMs(), remainingMs);
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw serviceUnavailable(exception);
        }
    }

    private void validateProperties() {
        if (properties.waitTimeoutMs() < 0
                || properties.leaseTimeoutMs() <= 0
                || properties.retryIntervalMs() <= 0) {
            throw new IllegalStateException("Invalid profile sync lock configuration");
        }
    }

    private static String lockKey(String partnerUserId) {
        if (partnerUserId == null || partnerUserId.isBlank()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return KEY_PREFIX + partnerUserId.trim();
    }

    private static ApiException serviceUnavailable(Throwable cause) {
        return cause == null
                ? new ApiException(ApiCode.SERVICE_UNAVAILABLE)
                : new ApiException(ApiCode.SERVICE_UNAVAILABLE, cause);
    }

    private static ThreadFactory daemonThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "profile-sync-lock-renewal");
            thread.setDaemon(true);
            return thread;
        };
    }

    @PreDestroy
    @Override
    public void close() {
        renewalExecutor.shutdownNow();
    }
}
