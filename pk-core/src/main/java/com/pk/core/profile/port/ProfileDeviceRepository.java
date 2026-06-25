package com.pk.core.profile.port;

import com.pk.core.profile.ProfileDeviceData;
import java.util.Optional;

public interface ProfileDeviceRepository {
    Optional<ProfileDeviceData> findByProfileId(long profileId);

    void upsert(ProfileDeviceData data);
}
