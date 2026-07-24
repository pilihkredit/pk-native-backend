package com.pk.core.profile.port;

import com.pk.core.profile.ProfileIdentityData;
import java.util.Optional;

public interface ProfileIdentityRepository {
    Optional<ProfileIdentityData> findByProfileId(long profileId);

    void upsert(ProfileIdentityData data);

    void updateLastLenderInteraction(long profileId, Long externalInteractionId);

    void scheduleRetentionAfterAccountClosure(long profileId, java.time.Instant retentionUntil);
}
