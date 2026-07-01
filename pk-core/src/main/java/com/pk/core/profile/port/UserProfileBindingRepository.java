package com.pk.core.profile.port;

public interface UserProfileBindingRepository {
    void recordLenderProfileSync(long profileId, String externalUserId);

    void updateKycStatus(long profileId, String kycStatus);
}
