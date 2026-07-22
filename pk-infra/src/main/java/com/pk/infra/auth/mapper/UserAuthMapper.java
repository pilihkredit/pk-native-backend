package com.pk.infra.auth.mapper;

import com.pk.core.auth.UserProfileSummary;
import com.pk.infra.auth.repository.PasswordCredentialRow;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAuthMapper {
    UserProfileSummary findByMobileNo(@Param("mobileNo") String mobileNo);

    UserProfileSummary findByProfileId(@Param("profileId") long profileId);

    UserProfileSummary findByPartnerUserId(@Param("partnerUserId") String partnerUserId);

    UserProfileSummary findLatestClosedByMobileNoForUpdate(@Param("mobileNo") String mobileNo);

    int relinquishPartnerUserId(
            @Param("profileId") long profileId,
            @Param("relinquishedPartnerUserId") String relinquishedPartnerUserId
    );

    int insertProfile(
            @Param("partnerUserId") String partnerUserId,
            @Param("mobileNo") String mobileNo
    );

    int saveSessionTokens(
            @Param("profileId") long profileId,
            @Param("accessToken") String accessToken,
            @Param("refreshToken") String refreshToken,
            @Param("accessTokenExpiresAt") Instant accessTokenExpiresAt
    );

    int saveAccessToken(
            @Param("profileId") long profileId,
            @Param("accessToken") String accessToken,
            @Param("accessTokenExpiresAt") Instant accessTokenExpiresAt
    );

    int clearSessionTokens(@Param("profileId") long profileId);

    int updateLastLoginAt(
            @Param("profileId") long profileId,
            @Param("lastLoginAt") Instant lastLoginAt
    );

    int updateLastLogoutAt(
            @Param("profileId") long profileId,
            @Param("lastLogoutAt") Instant lastLogoutAt
    );

    int countPasswordSet(@Param("profileId") long profileId);

    PasswordCredentialRow findPasswordCredential(@Param("profileId") long profileId);

    int savePassword(
            @Param("profileId") long profileId,
            @Param("passwordCiphertext") String passwordCiphertext,
            @Param("passwordNonce") byte[] passwordNonce,
            @Param("passwordTag") byte[] passwordTag
    );
}
