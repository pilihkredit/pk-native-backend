package com.pk.core.profile.port;

import com.pk.core.profile.ProfileAfData;
import java.util.Optional;

public interface ProfileAfRepository {
    Optional<ProfileAfData> findByRequestId(String requestId);

    long insert(ProfileAfData data);

    void updateLastLenderAudit(String requestId, String requestDataJson, String responseDataJson);
}
