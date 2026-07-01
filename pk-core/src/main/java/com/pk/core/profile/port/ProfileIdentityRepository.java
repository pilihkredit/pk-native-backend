package com.pk.core.profile.port;

import com.pk.core.profile.ProfileIdentityData;
import java.util.Optional;

public interface ProfileIdentityRepository {
    Optional<ProfileIdentityData> findByProfileId(long profileId);

    void upsert(ProfileIdentityData data);

    void updateLastLenderAudit(long profileId, String requestJson, String responseJson);
}
