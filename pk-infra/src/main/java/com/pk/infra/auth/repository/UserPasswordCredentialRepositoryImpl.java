package com.pk.infra.auth.repository;

import com.pk.core.auth.port.UserPasswordCredentialRepository;
import com.pk.infra.auth.mapper.UserPasswordCredentialMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserPasswordCredentialRepositoryImpl implements UserPasswordCredentialRepository {
    private final UserPasswordCredentialMapper userPasswordCredentialMapper;

    public UserPasswordCredentialRepositoryImpl(UserPasswordCredentialMapper userPasswordCredentialMapper) {
        this.userPasswordCredentialMapper = userPasswordCredentialMapper;
    }

    @Override
    public boolean isPasswordSet(long profileId) {
        return userPasswordCredentialMapper.countByProfileId(profileId) > 0;
    }

    @Override
    public Optional<PasswordCredential> findByProfileId(long profileId) {
        return Optional.ofNullable(userPasswordCredentialMapper.findByProfileId(profileId));
    }

    @Override
    public void insert(long profileId, String passwordHash) {
        userPasswordCredentialMapper.insert(profileId, passwordHash);
    }

    @Override
    public void recordFailedAttempt(long profileId, int failedAttempts, Instant lockedUntil) {
        userPasswordCredentialMapper.recordFailedAttempt(profileId, failedAttempts, lockedUntil);
    }

    @Override
    public void resetFailedAttempts(long profileId) {
        userPasswordCredentialMapper.resetFailedAttempts(profileId);
    }
}
