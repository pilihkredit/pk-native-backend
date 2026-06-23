package com.pk.infra.auth;

import com.pk.core.auth.OtpChallenge;
import com.pk.core.auth.port.OtpChallengeStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisOtpChallengeStore implements OtpChallengeStore {
    private static final String TOKEN_PREFIX = "auth:otp:token:";
    private static final String MOBILE_PREFIX = "auth:otp:mobile:";
    private static final String RESEND_DEVICE_PREFIX = "auth:otp:resend:device:";

    private final StringRedisTemplate redisTemplate;

    public RedisOtpChallengeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<OtpChallenge> findByToken(String otpToken) {
        String raw = redisTemplate.opsForValue().get(TOKEN_PREFIX + otpToken);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.split("\\|", 4);
        if (parts.length != 4) {
            return Optional.empty();
        }
        return Optional.of(new OtpChallenge(
                parts[0],
                parts[1],
                parts[2],
                Instant.parse(parts[3])
        ));
    }

    @Override
    public void save(String otpToken, OtpChallenge challenge, Duration ttl) {
        String value = challenge.mobileNo()
                + "|" + challenge.deviceNo()
                + "|" + challenge.otpCode()
                + "|" + challenge.expiresAt();
        redisTemplate.opsForValue().set(TOKEN_PREFIX + otpToken, value, ttl);
        redisTemplate.opsForValue().set(MOBILE_PREFIX + challenge.mobileNo(), otpToken, ttl);
    }

    @Override
    public void delete(String otpToken) {
        Optional<OtpChallenge> challenge = findByToken(otpToken);
        redisTemplate.delete(TOKEN_PREFIX + otpToken);
        challenge.ifPresent(value -> redisTemplate.delete(MOBILE_PREFIX + value.mobileNo()));
    }

    @Override
    public Optional<Duration> timeUntilResendAllowed(String deviceNo) {
        Long ttlSeconds = redisTemplate.getExpire(RESEND_DEVICE_PREFIX + deviceNo);
        if (ttlSeconds == null || ttlSeconds <= 0) {
            return Optional.empty();
        }
        return Optional.of(Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void markSent(String deviceNo, Duration resendInterval) {
        redisTemplate.opsForValue().set(RESEND_DEVICE_PREFIX + deviceNo, "1", resendInterval);
    }
}
