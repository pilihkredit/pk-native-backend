package com.pk.core.profile.port;

import com.pk.core.profile.ProfilePersonalData;
import java.util.Optional;

public interface ProfilePersonalRepository {
    Optional<ProfilePersonalData> findByUserId(long userId);

    void upsert(ProfilePersonalData data);

    void updateEmail(long userId, String userEmail);

    void updateLastLenderInteraction(long userId, Long externalInteractionId);
}
