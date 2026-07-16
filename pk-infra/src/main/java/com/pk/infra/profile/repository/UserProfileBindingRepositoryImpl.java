package com.pk.infra.profile.repository;

import com.pk.core.profile.IdentityDataRetention;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.infra.profile.mapper.UserProfileBindingMapper;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class UserProfileBindingRepositoryImpl implements UserProfileBindingRepository {
    private final UserProfileBindingMapper mapper;
    private final ProfileIdentityRepository profileIdentityRepository;

    public UserProfileBindingRepositoryImpl(
            UserProfileBindingMapper mapper,
            ProfileIdentityRepository profileIdentityRepository
    ) {
        this.mapper = mapper;
        this.profileIdentityRepository = profileIdentityRepository;
    }

    @Override
    public void recordLenderProfileSync(long profileId, String externalUserId) {
        if (externalUserId != null && !externalUserId.isBlank()) {
            mapper.recordLenderProfileSyncWithExternalUser(profileId, externalUserId.trim());
        } else {
            mapper.recordLenderProfileSyncWithoutExternalUser(profileId);
        }
    }

    @Override
    public void updateKycStatus(long profileId, String kycStatus) {
        mapper.updateKycStatus(profileId, kycStatus);
    }

    @Override
    public void closeAccount(long profileId) {
        Instant closedAt = Instant.now();
        Instant retentionUntil = IdentityDataRetention.retentionUntil(closedAt);
        mapper.closeAccount(profileId, closedAt, retentionUntil);
        profileIdentityRepository.scheduleRetentionAfterAccountClosure(profileId, retentionUntil);
    }
}
