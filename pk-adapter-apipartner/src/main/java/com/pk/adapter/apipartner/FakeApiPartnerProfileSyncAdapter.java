package com.pk.adapter.apipartner;

import com.pk.core.profile.port.LenderProfileSyncPort;

public class FakeApiPartnerProfileSyncAdapter implements LenderProfileSyncPort {
    @Override
    public LenderProfileSyncResult syncModule(LenderProfileSyncCommand command) {
        return new LenderProfileSyncResult("USR-FAKE", "{\"userId\":\"USR-FAKE\"}", null);
    }
}
