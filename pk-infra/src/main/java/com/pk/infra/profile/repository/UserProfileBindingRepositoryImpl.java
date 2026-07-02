package com.pk.infra.profile.repository;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.infra.profile.mapper.UserProfileBindingMapper;
import org.springframework.stereotype.Repository;
@Repository
public class UserProfileBindingRepositoryImpl implements UserProfileBindingRepository {
    private final UserProfileBindingMapper mapper;
    public UserProfileBindingRepositoryImpl(UserProfileBindingMapper mapper) { this.mapper = mapper; }
    @Override public void recordLenderProfileSync(long profileId, String externalUserId) {
        if (externalUserId != null && !externalUserId.isBlank()) {
            mapper.recordLenderProfileSyncWithExternalUser(profileId, externalUserId.trim());
        } else {
            mapper.recordLenderProfileSyncWithoutExternalUser(profileId);
        }
    }
    @Override public void updateKycStatus(long profileId, String kycStatus) { mapper.updateKycStatus(profileId, kycStatus); }
}
