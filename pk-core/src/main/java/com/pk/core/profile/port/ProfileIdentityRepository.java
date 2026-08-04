package com.pk.core.profile.port;

import com.pk.core.profile.ProfileIdentityData;
import java.util.Optional;

public interface ProfileIdentityRepository {
    Optional<ProfileIdentityData> findByUserId(long userId);

    void upsert(ProfileIdentityData data);

    void updateLastLenderInteraction(long userId, Long externalInteractionId);

}
