package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.port.OcrSessionStore;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisOcrSessionStore implements OcrSessionStore {
    private static final String KEY_PREFIX = "pk:ocr:session:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration sessionTtl;

    public RedisOcrSessionStore(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrProperties ocrProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.sessionTtl = ocrProperties.sessionTtl();
    }

    @Override
    public Optional<OcrSessionState> find(long profileId) {
        String value = redisTemplate.opsForValue().get(key(profileId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, OcrSessionState.class));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    @Override
    public void save(long profileId, OcrSessionState state) {
        try {
            redisTemplate.opsForValue().set(
                    key(profileId),
                    objectMapper.writeValueAsString(state),
                    sessionTtl
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to persist OCR session", exception);
        }
    }

    private static String key(long profileId) {
        return KEY_PREFIX + profileId;
    }
}
