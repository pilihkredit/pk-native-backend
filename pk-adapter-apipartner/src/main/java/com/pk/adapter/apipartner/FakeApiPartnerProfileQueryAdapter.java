package com.pk.adapter.apipartner;

import com.pk.core.profile.port.LenderProfileQueryPort;

public class FakeApiPartnerProfileQueryAdapter implements LenderProfileQueryPort {
    @Override
    public LenderProfileQueryResult query(LenderProfileQueryCommand command) {
        return new LenderProfileQueryResult("""
                {
                  "partnerUserId": "%s",
                  "userId": "USR-FAKE",
                  "mobileNo": {
                    "mobileNo": "081234567890"
                  }
                }
                """.formatted(command.partnerUserId()));
    }
}
