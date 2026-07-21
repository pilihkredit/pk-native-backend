package com.pk.core.profile.port;

import com.pk.core.profile.AccountCloseResult;

public interface UserProfileBindingRepository {
    void recordLenderProfileSync(long profileId, String externalUserId);

    void updateKycStatus(long profileId, String kycStatus);

    /**
     * Soft-close account: set {@code deleted_at} and {@code retention_until} (closure + 5 years)
     * in one update, clear session tokens on the profile row, and schedule identity retention.
     * Idempotent when already closed.
     */
    AccountCloseResult closeAccount(long profileId);
}
