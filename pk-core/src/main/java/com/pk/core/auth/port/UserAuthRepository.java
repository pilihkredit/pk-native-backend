package com.pk.core.auth.port;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.profile.EncryptedField;
import java.time.Instant;
import java.util.Optional;

public interface UserAuthRepository {
    Optional<UserProfileSummary> findByMobileNo(String mobileNo);

    Optional<UserProfileSummary> findByProfileId(long profileId);

    Optional<UserProfileSummary> findByPartnerUserId(String partnerUserId);

    UserProfileSummary createByMobileNo(String mobileNo);

    /**
     * Active profile by mobile, or create one. If only a soft-closed profile exists,
     * insert a new row reusing that profile's {@code partner_user_id} after renaming the closed row.
     */
    UserProfileSummary findOrCreateActiveByMobileNo(String mobileNo);

    void saveSessionTokens(
            long profileId,
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt
    );

    void saveAccessToken(long profileId, String accessToken, Instant accessTokenExpiresAt);

    void clearSessionTokens(long profileId);

    void updateLastLoginAt(long profileId, Instant lastLoginAt);

    void updateLastLogoutAt(long profileId, Instant lastLogoutAt);

    boolean isPasswordSet(long profileId);

    Optional<PasswordCredential> findPasswordCredential(long profileId);

    void savePassword(long profileId, EncryptedField password);

    record PasswordCredential(
            long profileId,
            EncryptedField password
    ) {
    }
}
