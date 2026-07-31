package com.pk.core.profile.port;

import com.pk.core.profile.ProfileLoginLogData;
import java.util.Optional;

public interface ProfileLoginLogRepository {
    Optional<ProfileLoginLogData> findByUserId(long userId);

    void upsert(ProfileLoginLogData data);

    void updateLastLenderInteraction(long userId, Long externalInteractionId);
}
