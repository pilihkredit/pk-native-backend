package com.pk.core.tracking.port;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

public interface TrackingEventRepository {
    Set<String> findExistingEventIds(Collection<String> eventIds);

    void insertBatch(Collection<TrackingEventInsert> events);

    record TrackingEventInsert(
            String eventId,
            String traceId,
            String partnerUserId,
            Long profileId,
            String eventType,
            Instant eventTime,
            String url,
            String deviceNo,
            String extendJson,
            String source
    ) {
    }
}
