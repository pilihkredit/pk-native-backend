package com.pk.infra.profile;

import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;

public record ProfileSyncJob(
        long profileId,
        String partnerUserId,
        String mobileNo,
        String requestId,
        ProfileSyncModule module,
        LenderDeviceContext device,
        ProfileSyncPayload payloadSnapshot
) {
    public static ProfileSyncJob fromStoredModule(
            long profileId,
            String partnerUserId,
            String mobileNo,
            String requestId,
            ProfileSyncModule module,
            LenderDeviceContext device
    ) {
        return new ProfileSyncJob(profileId, partnerUserId, mobileNo, requestId, module, device, null);
    }
}
