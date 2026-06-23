package com.pk.core.auth.port;

import com.pk.core.auth.OtpChallenge;
import java.time.Duration;
import java.util.Optional;

public interface OtpChallengeStore {
    Optional<OtpChallenge> findByToken(String otpToken);

    void save(String otpToken, OtpChallenge challenge, Duration ttl);

    void delete(String otpToken);

    Optional<Duration> timeUntilResendAllowed(String deviceNo);

    void markSent(String deviceNo, Duration resendInterval);
}
