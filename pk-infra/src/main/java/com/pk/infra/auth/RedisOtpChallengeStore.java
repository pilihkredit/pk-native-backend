package com.pk.infra.auth;

import com.pk.core.auth.OtpChallenge;
import com.pk.core.auth.port.OtpChallengeStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisOtpChallengeStore implements OtpChallengeStore {
    private final StringRedisTemplate redisTemplate;
    private final String tokenPrefix;
    private final String mobilePrefix;
    private final String resendDevicePrefix;

    public RedisOtpChallengeStore(StringRedisTemplate redisTemplate) {
        this(redisTemplate, "otp");
    }

    public RedisOtpChallengeStore(StringRedisTemplate redisTemplate, String namespace) {
        this.redisTemplate = redisTemplate;
        String ns = namespace == null || namespace.isBlank() ? "otp" : namespace.trim();
        this.tokenPrefix = "auth:" + ns + ":token:";
        this.mobilePrefix = "auth:" + ns + ":mobile:";
        this.resendDevicePrefix = "auth:" + ns + ":resend:device:";
    }

    @Override
    public Optional<OtpChallenge> findByToken(String otpToken) {
        String raw = redisTemplate.opsForValue().get(tokenPrefix + otpToken);
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
    public Optional<String> findTokenByMobile(String mobileNo) {
        if (mobileNo == null || mobileNo.isBlank()) {
            return Optional.empty();
        }
        String token = redisTemplate.opsForValue().get(mobilePrefix + mobileNo);
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(token);
    }

    @Override
    public void save(String otpToken, OtpChallenge challenge, Duration ttl) {
        String value = challenge.mobileNo()
                + "|" + challenge.deviceNo()
                + "|" + challenge.otpCode()
                + "|" + challenge.expiresAt();
        redisTemplate.opsForValue().set(tokenPrefix + otpToken, value, ttl);
        redisTemplate.opsForValue().set(mobilePrefix + challenge.mobileNo(), otpToken, ttl);
    }

    @Override
    public void delete(String otpToken) {
        Optional<OtpChallenge> challenge = findByToken(otpToken);
        redisTemplate.delete(tokenPrefix + otpToken);
        challenge.ifPresent(value -> redisTemplate.delete(mobilePrefix + value.mobileNo()));
    }

    @Override
    public Optional<Duration> timeUntilResendAllowed(String deviceNo) {
        Long ttlSeconds = redisTemplate.getExpire(resendDevicePrefix + deviceNo);
        if (ttlSeconds == null || ttlSeconds <= 0) {
            return Optional.empty();
        }
        return Optional.of(Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void markSent(String deviceNo, Duration resendInterval) {
        redisTemplate.opsForValue().set(resendDevicePrefix + deviceNo, "1", resendInterval);
    }
}
