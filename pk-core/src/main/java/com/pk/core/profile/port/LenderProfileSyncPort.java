package com.pk.core.profile.port;

import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;

public interface LenderProfileSyncPort {
    LenderProfileSyncResult syncModule(LenderProfileSyncCommand command);

    record LenderProfileSyncCommand(
            String requestId,
            String partnerUserId,
            ProfileSyncModule module,
            ProfileSyncPayload payload,
            LenderDeviceContext device
    ) {
    }

    record LenderProfileSyncResult(boolean success) {
    }
}
