package com.pk.core.task;

import java.util.Objects;

public record OutboxEvent(
        String eventId,
        WorkerQueue queue,
        String eventType,
        String aggregateId,
        String payloadJson
) {
    public OutboxEvent {
        Objects.requireNonNull(eventId, "eventId is required");
        Objects.requireNonNull(queue, "queue is required");
        Objects.requireNonNull(eventType, "eventType is required");
        Objects.requireNonNull(aggregateId, "aggregateId is required");
        Objects.requireNonNull(payloadJson, "payloadJson is required");
    }
}
