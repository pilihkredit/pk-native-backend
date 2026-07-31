package com.pk.core.profile.port;

import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.List;

public interface LenderProfileSyncPort {
    LenderProfileSyncResult syncModule(LenderProfileSyncCommand command);

    /**
     * Extra module applied into the same lender upsert {@code userInfo}.
     *
     * @param auditRequestId optional persistence key for module audit (e.g. AF row {@code request_id})
     */
    record SyncCompanion(
            ProfileSyncModule module,
            ProfileSyncPayload payload,
            String auditRequestId
    ) {
    }

    record LenderProfileSyncCommand(
            String requestId,
            String partnerUserId,
            String mobileNo,
            ProfileSyncModule module,
            ProfileSyncPayload payload,
            LenderDeviceContext device,
            List<SyncCompanion> companions
    ) {
        public LenderProfileSyncCommand {
            companions = companions == null ? List.of() : List.copyOf(companions);
        }

        public LenderProfileSyncCommand(
                String requestId,
                String partnerUserId,
                String mobileNo,
                ProfileSyncModule module,
                ProfileSyncPayload payload,
                LenderDeviceContext device
        ) {
            this(requestId, partnerUserId, mobileNo, module, payload, device, List.of());
        }
    }

    record LenderProfileSyncResult(String externalUserId, String responseDataJson, Long externalInteractionId) {
        public LenderProfileSyncResult(String externalUserId, String responseDataJson) {
            this(externalUserId, responseDataJson, null);
        }
    }
}
