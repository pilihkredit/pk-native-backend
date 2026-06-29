package com.pk.core.auth.port;

import com.pk.core.auth.UserProfileSummary;
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
}
