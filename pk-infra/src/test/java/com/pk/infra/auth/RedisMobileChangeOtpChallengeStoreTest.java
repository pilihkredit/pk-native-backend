package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.auth.MobileChangeOtpChallenge;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisMobileChangeOtpChallengeStoreTest {
    @Test
    void storesAndRestoresWhatsAppChannel() {
        RedisFixture fixture = fixture();
        Instant expiresAt = Instant.parse("2026-08-04T08:00:00Z");
        MobileChangeOtpChallenge challenge = new MobileChangeOtpChallenge(
                "otp-1", 7L, "81222222222", "device-1", "face-1", "123456", expiresAt, "WHATSAPP");

        fixture.store().save(challenge, Duration.ofMinutes(5));

        ArgumentCaptor<String> value = ArgumentCaptor.forClass(String.class);
        verify(fixture.values()).set(
                eq("auth:mobile-change:otp:token:otp-1"), value.capture(), eq(Duration.ofMinutes(5)));
        when(fixture.values().get("auth:mobile-change:otp:token:otp-1")).thenReturn(value.getValue());
        assertThat(fixture.store().findByToken("otp-1")).contains(challenge);
    }

    @Test
    void readsLegacyChallengeAsSms() {
        RedisFixture fixture = fixture();
        when(fixture.values().get("auth:mobile-change:otp:token:otp-1"))
                .thenReturn("7|81222222222|device-1|face-1|123456|2026-08-04T08:00:00Z");

        MobileChangeOtpChallenge challenge = fixture.store().findByToken("otp-1").orElseThrow();

        assertThat(challenge.channel()).isEqualTo("SMS");
    }

    private static RedisFixture fixture() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        return new RedisFixture(new RedisMobileChangeOtpChallengeStore(redis), values);
    }

    private record RedisFixture(
            RedisMobileChangeOtpChallengeStore store,
            ValueOperations<String, String> values
    ) {
    }
}
