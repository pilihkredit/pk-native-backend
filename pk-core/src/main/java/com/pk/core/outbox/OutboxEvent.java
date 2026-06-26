package com.pk.core.outbox;

import java.time.Instant;

public record OutboxEvent(
        long id,
        String eventNo,
        String eventType,
        String aggregateType,
        String aggregateId,
        String payloadJson,
        String status,
        int retryCount,
        Instant nextRetryAt
) {
}
