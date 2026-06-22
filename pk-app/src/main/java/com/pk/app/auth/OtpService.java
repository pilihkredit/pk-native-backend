package com.pk.app.auth;

import com.pk.app.config.AuthProperties;
import com.pk.core.error.AppBusinessException;
import com.pk.core.error.AppErrorCodes;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class OtpService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthProperties authProperties;
    private final Map<String, OtpChallenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastSentAt = new ConcurrentHashMap<>();

    public OtpService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public void enforceSendRateLimit(String mobileNo, String deviceNo) {
        enforceRateLimit(mobileNo, deviceNo);
    }

    public OtpSendResult send(String mobileNo, String deviceNo) {
        enforceRateLimit(mobileNo, deviceNo);
        String otpToken = "OTP_" + randomToken();
        String otpCode = resolveOtpCode();
        Instant expiresAt = Instant.now().plusSeconds(authProperties.otpExpireSeconds());
        challenges.put(otpToken, new OtpChallenge(mobileNo, deviceNo, otpCode, expiresAt));
        lastSentAt.put(rateLimitKey(mobileNo, deviceNo), Instant.now());
        return new OtpSendResult(
                otpToken,
                authProperties.otpExpireSeconds(),
                authProperties.otpResendSeconds()
        );
    }

    public void verify(String mobileNo, String deviceNo, String otpToken, String otpCode) {
        OtpChallenge challenge = challenges.get(otpToken);
        if (challenge == null) {
            throw new AppBusinessException(AppErrorCodes.INVALID_OTP);
        }
        if (!challenge.mobileNo().equals(mobileNo)) {
            throw new AppBusinessException(AppErrorCodes.OTP_TOKEN_MISMATCH);
        }
        if (!challenge.deviceNo().equals(deviceNo)) {
            throw new AppBusinessException(AppErrorCodes.INVALID_REQUEST);
        }
        if (challenge.expiresAt().isBefore(Instant.now()) || !challenge.otpCode().equals(otpCode)) {
            throw new AppBusinessException(AppErrorCodes.INVALID_OTP);
        }
        challenges.remove(otpToken);
    }

    void enforceRateLimit(String mobileNo, String deviceNo) {
        Instant last = lastSentAt.get(rateLimitKey(mobileNo, deviceNo));
        if (last != null) {
            long elapsed = Instant.now().getEpochSecond() - last.getEpochSecond();
            if (elapsed < authProperties.otpResendSeconds()) {
                throw new AppBusinessException(AppErrorCodes.TOO_MANY_REQUESTS);
            }
        }
    }

    private String resolveOtpCode() {
        String fixed = authProperties.otpFixedCode();
        if (fixed != null && !fixed.isBlank()) {
            return fixed;
        }
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private static String randomToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    private static String rateLimitKey(String mobileNo, String deviceNo) {
        return mobileNo + "|" + deviceNo;
    }

    record OtpChallenge(String mobileNo, String deviceNo, String otpCode, Instant expiresAt) {
    }

    public record OtpSendResult(String otpToken, int expireIn, int resendAfter) {
    }
}
