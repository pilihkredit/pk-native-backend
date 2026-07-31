package com.pk.core.external.port;

import com.pk.core.external.LenderInteractionLog;

public interface LenderInteractionLogRepository {
    long insert(LenderInteractionLog log);
}
