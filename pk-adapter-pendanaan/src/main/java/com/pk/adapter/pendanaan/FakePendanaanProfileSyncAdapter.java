package com.pk.adapter.pendanaan;

import com.pk.core.profile.port.LenderProfileSyncPort;

public class FakePendanaanProfileSyncAdapter implements LenderProfileSyncPort {
    @Override
    public LenderProfileSyncResult syncModule(LenderProfileSyncCommand command) {
        return new LenderProfileSyncResult(true);
    }
}
