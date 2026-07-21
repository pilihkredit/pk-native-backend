package com.pk.core.profile.port;

import com.pk.core.profile.ProfileTongdunData;
import java.util.Optional;

public interface ProfileTongdunRepository {
    Optional<ProfileTongdunData> findByRequestId(String requestId);

    long insert(ProfileTongdunData data);

    void updateLastLenderAudit(String requestId, String requestDataJson, String responseDataJson);
}
