package com.pk.core.profile.port;

import com.pk.core.profile.ProfileAfData;
import java.util.Optional;

public interface ProfileAfRepository {
    Optional<ProfileAfData> findByRequestId(String requestId);

    Optional<ProfileAfData> findLatestByDeviceNo(String deviceNo);

    long insert(ProfileAfData data);

    void updateLastLenderInteraction(String requestId, Long externalInteractionId);

    /** Backfill profile/mobile when AF was saved before login. */
    void bindProfileIfNull(long id, long profileId, String mobileNo);
}
