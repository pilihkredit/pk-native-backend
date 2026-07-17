package com.pk.infra.auth.repository;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.EncryptedField;
import com.pk.infra.auth.mapper.UserAuthMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserAuthRepositoryImpl implements UserAuthRepository {
    private final UserAuthMapper userAuthMapper;

    public UserAuthRepositoryImpl(UserAuthMapper userAuthMapper) {
        this.userAuthMapper = userAuthMapper;
    }

    @Override
    public Optional<UserProfileSummary> findByMobileNo(String mobileNo) {
        return Optional.ofNullable(userAuthMapper.findByMobileNo(mobileNo));
    }

    @Override
    public Optional<UserProfileSummary> findByProfileId(long profileId) {
        return Optional.ofNullable(userAuthMapper.findByProfileId(profileId));
    }

    @Override
    public UserProfileSummary createByMobileNo(String mobileNo) {
        String partnerUserId = "U" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        userAuthMapper.insertProfile(partnerUserId, mobileNo);
        return findByMobileNo(mobileNo)
                .map(profile -> new UserProfileSummary(
                        profile.profileId(),
                        profile.partnerUserId(),
                        profile.mobileNo(),
                        true
                ))
                .orElseThrow(() -> new IllegalStateException("Failed to load created user profile"));
    }

    @Override
    public void saveSessionTokens(
            long profileId,
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt
    ) {
        userAuthMapper.saveSessionTokens(profileId, accessToken, refreshToken, accessTokenExpiresAt);
    }

    @Override
    public void saveAccessToken(long profileId, String accessToken, Instant accessTokenExpiresAt) {
        userAuthMapper.saveAccessToken(profileId, accessToken, accessTokenExpiresAt);
    }

    @Override
    public void clearSessionTokens(long profileId) {
        userAuthMapper.clearSessionTokens(profileId);
    }

    @Override
    public boolean isPasswordSet(long profileId) {
        return userAuthMapper.countPasswordSet(profileId) > 0;
    }

    @Override
    public Optional<PasswordCredential> findPasswordCredential(long profileId) {
        return Optional.ofNullable(userAuthMapper.findPasswordCredential(profileId)).map(this::toCredential);
    }

    @Override
    public void savePassword(long profileId, EncryptedField password) {
        userAuthMapper.savePassword(
                profileId,
                password.ciphertextBase64(),
                password.nonce(),
                password.tag()
        );
    }

    @Override
    public void recordPasswordFailedAttempt(long profileId, int failedAttempts, Instant lockedUntil) {
        userAuthMapper.recordPasswordFailedAttempt(profileId, failedAttempts, lockedUntil);
    }

    @Override
    public void resetPasswordFailedAttempts(long profileId) {
        userAuthMapper.resetPasswordFailedAttempts(profileId);
    }

    private PasswordCredential toCredential(PasswordCredentialRow row) {
        return new PasswordCredential(
                row.profileId(),
                new EncryptedField(row.passwordCiphertext(), row.passwordNonce(), row.passwordTag()),
                row.failedAttempts(),
                row.lockedUntil()
        );
    }
}
