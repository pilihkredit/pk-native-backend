package com.pk.core.external.port;

import com.pk.core.external.LenderInteractionLog;

public interface LenderInteractionLogRepository {
    void insert(LenderInteractionLog log);
}
