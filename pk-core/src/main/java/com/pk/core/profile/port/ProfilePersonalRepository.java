package com.pk.core.profile.port;

import com.pk.core.profile.ProfilePersonalData;
import java.util.Optional;

public interface ProfilePersonalRepository {
    Optional<ProfilePersonalData> findByProfileId(long profileId);

    void upsert(ProfilePersonalData data);

    void updateEmail(long profileId, String userEmail);

    void updateLastLenderAudit(long profileId, String requestDataJson, String responseDataJson);
}
