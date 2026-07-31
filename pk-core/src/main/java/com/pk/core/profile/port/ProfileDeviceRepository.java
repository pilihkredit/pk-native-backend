package com.pk.core.profile.port;

import com.pk.core.profile.ProfileDeviceData;
import java.util.Optional;

public interface ProfileDeviceRepository {
    boolean existsByUserId(long userId);

    Optional<ProfileDeviceData> findByDeviceNo(String deviceNo);

    void upsertByDeviceNo(ProfileDeviceData data);
}
