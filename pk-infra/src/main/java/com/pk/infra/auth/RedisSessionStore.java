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
    public Optional<AuthSession> findByProfileId(long profileId) {
        String raw = redisTemplate.opsForValue().get(key(profileId));
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.split("\\|", 5);
        if (parts.length != 5) {
            return Optional.empty();
        }
        return Optional.of(new AuthSession(
                profileId,
                Long.parseLong(parts[0]),
                parts[1],
                parts[2],
                Instant.parse(parts[3])
        ));
    }

    @Override
    public void save(long profileId, AuthSession session, Duration ttl) {
        String value = session.sessionVersion()
                + "|" + session.deviceId()
                + "|" + session.loginChannel()
                + "|" + session.issuedAt().toString()
                + "|";
        redisTemplate.opsForValue().set(key(profileId), value, ttl);
    }

    @Override
    public void delete(long profileId) {
        redisTemplate.delete(key(profileId));
    }

    private static String key(long profileId) {
        return KEY_PREFIX + profileId;
    }
}
