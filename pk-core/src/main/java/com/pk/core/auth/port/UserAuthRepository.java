package com.pk.core.auth.port;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.profile.EncryptedField;
import java.time.Instant;
import java.util.Optional;

public interface UserAuthRepository {
    Optional<UserProfileSummary> findByMobileNo(String mobileNo);

    Optional<UserProfileSummary> findByProfileId(long profileId);

    UserProfileSummary createByMobileNo(String mobileNo);

    void saveSessionTokens(
            long profileId,
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt
    );

    void saveAccessToken(long profileId, String accessToken, Instant accessTokenExpiresAt);

    void clearSessionTokens(long profileId);

    boolean isPasswordSet(long profileId);

    Optional<PasswordCredential> findPasswordCredential(long profileId);

    void savePassword(long profileId, EncryptedField password);

    void recordPasswordFailedAttempt(long profileId, int failedAttempts, Instant lockedUntil);

    void resetPasswordFailedAttempts(long profileId);

    record PasswordCredential(
            long profileId,
            EncryptedField password,
            int failedAttempts,
            Instant lockedUntil
    ) {
    }
}
