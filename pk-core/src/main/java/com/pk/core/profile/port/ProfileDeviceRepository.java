package com.pk.core.profile.port;

import com.pk.core.profile.ProfileDeviceData;
import java.util.Optional;

public interface ProfileDeviceRepository {
    boolean existsByUserId(long userId);

    Optional<ProfileDeviceData> findByDeviceNo(String deviceNo);

    /** Most recently updated device row for the user (for lender openUserDevice). */
    Optional<ProfileDeviceData> findLatestByUserId(long userId);

    void upsertByDeviceNo(ProfileDeviceData data);
}
