package com.pk.infra.auth.repository;

import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.EncryptedField;
import com.pk.infra.auth.mapper.UserAuthMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<UserProfileSummary> findActiveByMobileNoExcludingUserId(String mobileNo, long userId) {
        return Optional.ofNullable(userAuthMapper.findActiveByMobileNoExcludingUserId(mobileNo, userId));
    }

    @Override
    public Optional<UserProfileSummary> findByUserId(long userId) {
        return Optional.ofNullable(userAuthMapper.findByUserId(userId));
    }

    @Override
    public Optional<UserProfileSummary> findByPartnerUserId(String partnerUserId) {
        return Optional.ofNullable(userAuthMapper.findByPartnerUserId(partnerUserId));
    }

    @Override
    public UserProfileSummary createByMobileNo(String mobileNo) {
        String partnerUserId = "U" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        userAuthMapper.insertProfile(partnerUserId, mobileNo);
        return findByMobileNo(mobileNo)
                .map(profile -> new UserProfileSummary(
                        profile.userId(),
                        profile.partnerUserId(),
                        profile.mobileNo(),
                        true
                ))
                .orElseThrow(() -> new IllegalStateException("Failed to load created user profile"));
    }

    @Override
    @Transactional
    public UserProfileSummary findOrCreateActiveByMobileNo(String mobileNo) {
        Optional<UserProfileSummary> active = findByMobileNo(mobileNo);
        if (active.isPresent()) {
            return active.get();
        }

        UserProfileSummary closed = userAuthMapper.findLatestClosedByMobileNoForUpdate(mobileNo);
        active = findByMobileNo(mobileNo);
        if (active.isPresent()) {
            return active.get();
        }
        if (closed == null) {
            return createByMobileNo(mobileNo);
        }

        String originalPartnerUserId = closed.partnerUserId();
        String relinquished = originalPartnerUserId + "_closed_" + closed.userId();
        int updated = userAuthMapper.relinquishPartnerUserId(closed.userId(), relinquished);
        if (updated != 1) {
            return findByMobileNo(mobileNo).orElseGet(() -> createByMobileNo(mobileNo));
        }

        userAuthMapper.insertProfile(originalPartnerUserId, mobileNo);
        return findByMobileNo(mobileNo)
                .map(profile -> new UserProfileSummary(
                        profile.userId(),
                        profile.partnerUserId(),
                        profile.mobileNo(),
                        true
                ))
                .orElseThrow(() -> new IllegalStateException("Failed to load re-created user profile"));
    }

    @Override
    public void saveSessionTokens(
            long userId,
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt
    ) {
        userAuthMapper.saveSessionTokens(userId, accessToken, refreshToken, accessTokenExpiresAt);
    }

    @Override
    public void saveAccessToken(long userId, String accessToken, Instant accessTokenExpiresAt) {
        userAuthMapper.saveAccessToken(userId, accessToken, accessTokenExpiresAt);
    }

    @Override
    public void clearSessionTokens(long userId) {
        userAuthMapper.clearSessionTokens(userId);
    }

    @Override
    public void updateMobileNo(long userId, String mobileNo) {
        userAuthMapper.updateMobileNo(userId, mobileNo);
    }

    @Override
    public void updateLastLoginAt(long userId, Instant lastLoginAt) {
        userAuthMapper.updateLastLoginAt(userId, lastLoginAt);
    }

    @Override
    public void updateLastLogoutAt(long userId, Instant lastLogoutAt) {
        userAuthMapper.updateLastLogoutAt(userId, lastLogoutAt);
    }

    @Override
    public boolean isPasswordSet(long userId) {
        return userAuthMapper.countPasswordSet(userId) > 0;
    }

    @Override
    public Optional<PasswordCredential> findPasswordCredential(long userId) {
        return Optional.ofNullable(userAuthMapper.findPasswordCredential(userId)).map(this::toCredential);
    }

    @Override
    public void savePassword(long userId, EncryptedField password) {
        userAuthMapper.savePassword(
                userId,
                password.ciphertextBase64(),
                password.nonce(),
                password.tag()
        );
    }

    private PasswordCredential toCredential(PasswordCredentialRow row) {
        return new PasswordCredential(
                row.userId(),
                new EncryptedField(row.passwordCiphertext(), row.passwordNonce(), row.passwordTag())
        );
    }
}
