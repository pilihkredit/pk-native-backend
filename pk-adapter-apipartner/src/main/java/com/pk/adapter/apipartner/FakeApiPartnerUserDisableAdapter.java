package com.pk.adapter.apipartner;

import com.pk.core.auth.port.LenderUserDisablePort;

public class FakeApiPartnerUserDisableAdapter implements LenderUserDisablePort {
    @Override
    public void disableUser(String partnerUserId) {
        // no-op for local / disabled ApiPartner
    }
}
