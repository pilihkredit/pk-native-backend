package com.pk.core.callback.port;

import java.time.Instant;
import java.util.Optional;

public interface CallbackEventRepository {
    long insert(CallbackEventInsert insert);

    Optional<CallbackEventRecord> findById(long id);

    Optional<CallbackEventRecord> findByIdempotencyKey(String idempotencyKey);

    void markProcessed(long id, Instant processedAt);

    void markIgnored(long id, Instant processedAt);

    record CallbackEventInsert(
            String callbackNo,
            String providerCode,
            String callbackType,
            String businessId,
            String idempotencyKey,
            String externalStatus,
            String payloadJson,
            String processStatus,
            Instant receivedAt
    ) {
    }

    record CallbackEventRecord(
            long id,
            String callbackNo,
            String providerCode,
            String callbackType,
            String businessId,
            String idempotencyKey,
            String externalStatus,
            String payloadJson,
            String processStatus
    ) {
    }
}
