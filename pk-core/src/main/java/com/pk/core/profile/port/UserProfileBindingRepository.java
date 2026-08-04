package com.pk.core.profile.port;

public interface UserProfileBindingRepository {
    void recordLenderProfileSync(long userId, String externalUserId);

    void updateKycStatus(long userId, String kycStatus);
}
