package com.pk.core.profile.port;

import java.util.List;

public interface LenderProfileQueryPort {
    LenderProfileQueryResult query(LenderProfileQueryCommand command);

    record LenderProfileQueryCommand(String partnerUserId, List<String> modules) {
    }

    record LenderProfileQueryResult(String rawResponseJson) {
    }
}
