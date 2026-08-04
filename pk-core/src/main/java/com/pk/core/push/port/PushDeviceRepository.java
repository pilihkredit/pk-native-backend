package com.pk.core.push.port;

import com.pk.core.push.PushDeviceRegistration;

public interface PushDeviceRepository {
    void register(PushDeviceRegistration registration);

    void bindUser(long userId, String deviceNo);

    void unbindUser(long userId, String deviceNo);
}
