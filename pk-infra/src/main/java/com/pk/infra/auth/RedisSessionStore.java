package com.pk.infra.auth;

import com.pk.core.auth.AuthSession;
import com.pk.core.auth.port.SessionStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisSessionStore implements SessionStore {
    private static final String KEY_PREFIX = "auth:session:";

    private final StringRedisTemplate redisTemplate;

    public RedisSessionStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<AuthSession> findByUserId(long userId) {
        String raw = redisTemplate.opsForValue().get(key(userId));
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.split("\\|", 5);
        // Expected: version|deviceId|loginChannel|issuedAt| (trailing empty) or without trailing pipe.
        if (parts.length < 4) {
            return Optional.empty();
        }
        return Optional.of(new AuthSession(
                userId,
                Long.parseLong(parts[0]),
                parts[1],
                parts[2],
                Instant.parse(parts[3])
        ));
    }

    @Override
    public void save(long userId, AuthSession session, Duration ttl) {
        String value = session.sessionVersion()
                + "|" + session.deviceId()
                + "|" + session.loginChannel()
                + "|" + session.issuedAt().toString()
                + "|";
        redisTemplate.opsForValue().set(key(userId), value, ttl);
    }

    @Override
    public void delete(long userId) {
        redisTemplate.delete(key(userId));
    }

    private static String key(long userId) {
        return KEY_PREFIX + userId;
    }
}
