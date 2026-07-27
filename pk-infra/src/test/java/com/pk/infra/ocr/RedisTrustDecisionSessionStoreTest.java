package com.pk.infra.ocr;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.ocr.TrustDecisionSessionState;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisTrustDecisionSessionStoreTest {
    @Test
    void savesWithProviderQualifiedKey() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        TrustDecisionProperties properties = new TrustDecisionProperties();
        properties.setSessionTtl(Duration.ofMinutes(30));
        RedisTrustDecisionSessionStore store = new RedisTrustDecisionSessionStore(
                redis,
                new ObjectMapper().findAndRegisterModules(),
                properties
        );

        store.save(42L, new TrustDecisionSessionState(
                false, false, null, null, null, null, null, null, null, null
        ));

        verify(values).set(
                eq("pk:ocr:session:trustDecision:42"),
                anyString(),
                eq(Duration.ofMinutes(30))
        );
    }
}
