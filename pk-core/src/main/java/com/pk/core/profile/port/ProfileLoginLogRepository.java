package com.pk.core.profile.port;

import com.pk.core.profile.ProfileLoginLogData;
import java.util.Optional;

public interface ProfileLoginLogRepository {
    Optional<ProfileLoginLogData> findByProfileId(long profileId);

    void upsert(ProfileLoginLogData data);

    void updateLastLenderInteraction(long profileId, Long externalInteractionId);
}
