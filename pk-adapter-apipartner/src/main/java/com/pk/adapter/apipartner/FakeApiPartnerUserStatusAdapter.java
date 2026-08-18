package com.pk.adapter.apipartner;

import com.pk.core.home.port.LenderUserStatusPort;

public class FakeApiPartnerUserStatusAdapter implements LenderUserStatusPort {
    @Override
    public LenderUserStatusResult queryStatus(LenderUserStatusCommand command) {
        return new LenderUserStatusResult(
                command.partnerUserId(),
                "USR-FAKE",
                2,
                11,
                null,
                true,
                true,
                true,
                0,
                null,
                false,
                null
        );
    }
}
