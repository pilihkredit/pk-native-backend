package com.pk.infra.auth;

import com.pk.core.auth.port.RefreshTokenStore;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisRefreshTokenStore implements RefreshTokenStore {
    private static final String TOKEN_PREFIX = "auth:refresh:";
    private static final String PROFILE_INDEX_PREFIX = "auth:refresh:profile:";

    private final StringRedisTemplate redisTemplate;

    public RedisRefreshTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String refreshTokenId, RefreshTokenRecord record, Duration ttl) {
        String value = record.userId() + "|" + record.sessionVersion() + "|" + record.deviceId();
        redisTemplate.opsForValue().set(TOKEN_PREFIX + refreshTokenId, value, ttl);
        redisTemplate.opsForSet().add(profileIndex(record.userId()), refreshTokenId);
        redisTemplate.expire(profileIndex(record.userId()), ttl);
    }

    @Override
    public Optional<RefreshTokenRecord> find(String refreshTokenId) {
        String raw = redisTemplate.opsForValue().get(TOKEN_PREFIX + refreshTokenId);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.split("\\|", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }
        return Optional.of(new RefreshTokenRecord(
                Long.parseLong(parts[0]),
                Long.parseLong(parts[1]),
                parts[2]
        ));
    }

    @Override
    public void delete(String refreshTokenId) {
        Optional<RefreshTokenRecord> record = find(refreshTokenId);
        redisTemplate.delete(TOKEN_PREFIX + refreshTokenId);
        record.ifPresent(value -> redisTemplate.opsForSet().remove(profileIndex(value.userId()), refreshTokenId));
    }

    @Override
    public void deleteAllForProfile(long userId) {
        Set<String> tokenIds = redisTemplate.opsForSet().members(profileIndex(userId));
        if (tokenIds != null) {
            for (String tokenId : tokenIds) {
                redisTemplate.delete(TOKEN_PREFIX + tokenId);
            }
        }
        redisTemplate.delete(profileIndex(userId));
    }

    private static String profileIndex(long userId) {
        return PROFILE_INDEX_PREFIX + userId;
    }
}
