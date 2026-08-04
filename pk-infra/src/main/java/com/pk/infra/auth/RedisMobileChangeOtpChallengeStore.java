package com.pk.infra.auth;

import com.pk.core.auth.MobileChangeOtpChallenge;
import com.pk.core.auth.port.MobileChangeOtpChallengeStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisMobileChangeOtpChallengeStore implements MobileChangeOtpChallengeStore {
    private static final String TOKEN_PREFIX = "auth:mobile-change:otp:token:";
    private static final String RESEND_PREFIX = "auth:mobile-change:otp:resend:device:";
    private final StringRedisTemplate redisTemplate;

    public RedisMobileChangeOtpChallengeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<MobileChangeOtpChallenge> findByToken(String otpToken) {
        String raw = redisTemplate.opsForValue().get(TOKEN_PREFIX + otpToken);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String[] parts = raw.split("\\|", 6);
        if (parts.length != 6) {
            return Optional.empty();
        }
        try {
            return Optional.of(new MobileChangeOtpChallenge(
                    otpToken,
                    Long.parseLong(parts[0]),
                    parts[1],
                    parts[2],
                    parts[3],
                    parts[4],
                    Instant.parse(parts[5])
            ));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void save(MobileChangeOtpChallenge challenge, Duration ttl) {
        String value = challenge.userId()
                + "|" + challenge.newMobileNo()
                + "|" + challenge.deviceNo()
                + "|" + challenge.faceVerifyToken()
                + "|" + challenge.otpCode()
                + "|" + challenge.expiresAt();
        redisTemplate.opsForValue().set(TOKEN_PREFIX + challenge.otpToken(), value, ttl);
    }

    @Override
    public void delete(String otpToken) {
        redisTemplate.delete(TOKEN_PREFIX + otpToken);
    }

    @Override
    public Optional<Duration> timeUntilResendAllowed(String deviceNo) {
        Long seconds = redisTemplate.getExpire(RESEND_PREFIX + deviceNo);
        return seconds == null || seconds <= 0 ? Optional.empty() : Optional.of(Duration.ofSeconds(seconds));
    }

    @Override
    public void markSent(String deviceNo, Duration resendInterval) {
        redisTemplate.opsForValue().set(RESEND_PREFIX + deviceNo, "1", resendInterval);
    }
}
