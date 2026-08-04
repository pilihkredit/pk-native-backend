package com.pk.infra.auth.mapper;

import com.pk.core.auth.UserProfileSummary;
import com.pk.infra.auth.repository.PasswordCredentialRow;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAuthMapper {
    UserProfileSummary findByMobileNo(@Param("mobileNo") String mobileNo);

    UserProfileSummary findActiveByMobileNoExcludingUserId(
            @Param("mobileNo") String mobileNo,
            @Param("userId") long userId
    );

    UserProfileSummary findByUserId(@Param("userId") long userId);

    UserProfileSummary findByPartnerUserId(@Param("partnerUserId") String partnerUserId);

    UserProfileSummary findLatestClosedByMobileNoForUpdate(@Param("mobileNo") String mobileNo);

    int relinquishPartnerUserId(
            @Param("userId") long userId,
            @Param("relinquishedPartnerUserId") String relinquishedPartnerUserId
    );

    int insertProfile(
            @Param("partnerUserId") String partnerUserId,
            @Param("mobileNo") String mobileNo
    );

    int saveSessionTokens(
            @Param("userId") long userId,
            @Param("accessToken") String accessToken,
            @Param("refreshToken") String refreshToken,
            @Param("accessTokenExpiresAt") Instant accessTokenExpiresAt
    );

    int saveAccessToken(
            @Param("userId") long userId,
            @Param("accessToken") String accessToken,
            @Param("accessTokenExpiresAt") Instant accessTokenExpiresAt
    );

    int clearSessionTokens(@Param("userId") long userId);

    int updateMobileNo(@Param("userId") long userId, @Param("mobileNo") String mobileNo);

    int updateLastLoginAt(
            @Param("userId") long userId,
            @Param("lastLoginAt") Instant lastLoginAt
    );

    int updateLastLogoutAt(
            @Param("userId") long userId,
            @Param("lastLogoutAt") Instant lastLogoutAt
    );

    int countPasswordSet(@Param("userId") long userId);

    PasswordCredentialRow findPasswordCredential(@Param("userId") long userId);

    int savePassword(
            @Param("userId") long userId,
            @Param("passwordCiphertext") String passwordCiphertext,
            @Param("passwordNonce") byte[] passwordNonce,
            @Param("passwordTag") byte[] passwordTag
    );

    int changePassword(
            @Param("userId") long userId,
            @Param("passwordCiphertext") String passwordCiphertext,
            @Param("passwordNonce") byte[] passwordNonce,
            @Param("passwordTag") byte[] passwordTag
    );
}
