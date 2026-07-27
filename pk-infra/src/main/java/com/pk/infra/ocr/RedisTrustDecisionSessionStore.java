package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.ocr.TrustDecisionSessionState;
import com.pk.core.profile.port.TrustDecisionSessionStore;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisTrustDecisionSessionStore implements TrustDecisionSessionStore {
    private static final String KEY_PREFIX = "pk:ocr:session:trustDecision:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final java.util.function.Supplier<Duration> sessionTtlSupplier;

    public RedisTrustDecisionSessionStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            TrustDecisionProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.sessionTtlSupplier = properties::sessionTtl;
    }

    public RedisTrustDecisionSessionStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrProviderConfigLoader configLoader
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.sessionTtlSupplier = () -> configLoader.loadTrustDecision().sessionTtl();
    }

    @Override
    public Optional<TrustDecisionSessionState> find(long userId) {
        String value = redisTemplate.opsForValue().get(key(userId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, TrustDecisionSessionState.class));
        } catch (Exception exception) {
            redisTemplate.delete(key(userId));
            return Optional.empty();
        }
    }

    @Override
    public void save(long userId, TrustDecisionSessionState state) {
        try {
        redisTemplate.opsForValue().set(
                key(userId), objectMapper.writeValueAsString(state), sessionTtlSupplier.get());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to persist TrustDecision session", exception);
        }
    }

    @Override
    public void delete(long userId) {
        redisTemplate.delete(key(userId));
    }

    private static String key(long userId) {
        return KEY_PREFIX + userId;
    }
}
