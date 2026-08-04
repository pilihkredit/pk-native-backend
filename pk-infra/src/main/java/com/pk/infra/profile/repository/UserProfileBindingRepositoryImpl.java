package com.pk.infra.profile.repository;

import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.infra.profile.mapper.UserProfileBindingMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserProfileBindingRepositoryImpl implements UserProfileBindingRepository {
    private final UserProfileBindingMapper mapper;

    public UserProfileBindingRepositoryImpl(UserProfileBindingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void recordLenderProfileSync(long userId, String externalUserId) {
        if (externalUserId != null && !externalUserId.isBlank()) {
            mapper.recordLenderProfileSyncWithExternalUser(userId, externalUserId.trim());
        } else {
            mapper.recordLenderProfileSyncWithoutExternalUser(userId);
        }
    }

    @Override
    public void updateKycStatus(long userId, String kycStatus) {
        mapper.updateKycStatus(userId, kycStatus);
    }
}
