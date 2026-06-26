package com.pk.core.profile.port;

import com.pk.core.profile.ProfileWorkData;
import java.util.Optional;

public interface ProfileWorkRepository {
    Optional<ProfileWorkData> findByProfileId(long profileId);

    void upsert(ProfileWorkData data);
}
