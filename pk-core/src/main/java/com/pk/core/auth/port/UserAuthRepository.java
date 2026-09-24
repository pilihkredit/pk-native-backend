package com.pk.core.auth.port;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.profile.EncryptedField;
import java.time.Instant;
import java.util.Optional;

public interface UserAuthRepository {
    Optional<UserProfileSummary> findByMobileNo(String mobileNo);

    Optional<UserProfileSummary> findActiveByMobileNoExcludingUserId(String mobileNo, long userId);

    Optional<UserProfileSummary> findByUserId(long userId);

    Optional<UserProfileSummary> findByPartnerUserId(String partnerUserId);

    UserProfileSummary createByMobileNo(String mobileNo);

    /**
     * Active profile by mobile, or create one with a new {@code partner_user_id}.
     * If only a soft-closed profile exists, locks that row for concurrency then inserts a fresh profile.
     */
    UserProfileSummary findOrCreateActiveByMobileNo(String mobileNo);

    void saveSessionTokens(
            long userId,
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt
    );

    void saveAccessToken(long userId, String accessToken, Instant accessTokenExpiresAt);

    void clearSessionTokens(long userId);

    void updateMobileNo(long userId, String mobileNo);

    void updateLastLoginAt(long userId, Instant lastLoginAt);

    Optional<String> findLastLoginDeviceNo(long userId);

    void updateLastLoginDeviceNo(long userId, String deviceNo);

    void updateLastLogoutAt(long userId, Instant lastLogoutAt);

    boolean isPasswordSet(long userId);

    Optional<PasswordCredential> findPasswordCredential(long userId);

    void savePassword(long userId, EncryptedField password);

    void changePassword(long userId, EncryptedField password);

    record PasswordCredential(
            long userId,
            EncryptedField password
    ) {
    }
}
