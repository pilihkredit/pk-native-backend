package com.pk.core.push.port;

import com.pk.core.push.PushDeviceRegistration;
import com.pk.core.push.PushDeviceTarget;
import java.util.List;

public interface PushDeviceRepository {
    void register(PushDeviceRegistration registration);

    void bindUser(long userId, String deviceNo);

    void unbindUser(long userId, String deviceNo);

    /** All-device keyset page ordered by id, for full-audience delivery scans. */
    List<PushDeviceTarget> findTargetsAfterId(long idExclusive, int limit);

    /** Devices currently bound to one user. */
    List<PushDeviceTarget> findTargetsByUserId(long userId);
}
