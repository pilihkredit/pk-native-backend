package com.pk.core.outbox.port;

import com.pk.core.outbox.OutboxEvent;
import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository {
    void insertPending(OutboxEventDraft draft);

    List<OutboxEvent> findReady(int limit);

    void markCompleted(long id);

    void markFailed(long id, int retryCount, Instant nextRetryAt);

    void markTerminalFailure(long id, int retryCount);

    record OutboxEventDraft(
            String eventNo,
            String eventType,
            String aggregateType,
            String aggregateId,
            String payloadJson
    ) {
    }
}
