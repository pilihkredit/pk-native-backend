package com.pk.core.auth.port;

import com.pk.core.auth.MobileChangeOtpChallenge;
import java.time.Duration;
import java.util.Optional;

public interface MobileChangeOtpChallengeStore {
    Optional<MobileChangeOtpChallenge> findByToken(String otpToken);

    void save(MobileChangeOtpChallenge challenge, Duration ttl);

    void delete(String otpToken);

    Optional<Duration> timeUntilResendAllowed(String deviceNo);

    void markSent(String deviceNo, Duration resendInterval);
}
