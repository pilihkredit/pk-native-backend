package com.pk.adapter.pendanaan;

import com.pk.core.profile.port.LenderProfileQueryPort;

public class FakePendanaanProfileQueryAdapter implements LenderProfileQueryPort {
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
