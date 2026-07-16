package com.pk.core.profile.port;

public interface UserProfileBindingRepository {
    void recordLenderProfileSync(long profileId, String externalUserId);

    void updateKycStatus(long profileId, String kycStatus);

    /**
     * Soft-close account and schedule sensitive data retention (closure time + 5 years).
     */
    void closeAccount(long profileId);
}
