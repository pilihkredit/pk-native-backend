package com.pk.infra.profile;

import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.List;

public record ProfileSyncJob(
        long userId,
        String partnerUserId,
        String mobileNo,
        String requestId,
        ProfileSyncModule module,
        LenderDeviceContext device,
        ProfileSyncPayload payloadSnapshot,
        List<LenderProfileSyncPort.SyncCompanion> companions
) {
    public ProfileSyncJob {
        companions = companions == null ? List.of() : List.copyOf(companions);
    }

    public ProfileSyncJob(
            long userId,
            String partnerUserId,
            String mobileNo,
            String requestId,
            ProfileSyncModule module,
            LenderDeviceContext device,
            ProfileSyncPayload payloadSnapshot
    ) {
        this(userId, partnerUserId, mobileNo, requestId, module, device, payloadSnapshot, List.of());
    }

    public static ProfileSyncJob fromStoredModule(
            long userId,
            String partnerUserId,
            String mobileNo,
            String requestId,
            ProfileSyncModule module,
            LenderDeviceContext device
    ) {
        return new ProfileSyncJob(userId, partnerUserId, mobileNo, requestId, module, device, null, List.of());
    }
}
