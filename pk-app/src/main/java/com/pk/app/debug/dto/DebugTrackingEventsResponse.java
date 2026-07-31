package com.pk.app.debug.dto;

import java.time.Instant;
import java.util.List;

public record DebugTrackingEventsResponse(
        boolean found,
        String clientNo,
        String mobileNo,
        String requestedUserId,
        Long userId,
        String partnerUserId,
        long total,
        int returned,
        boolean truncated,
        List<EventTypeSummary> eventTypes,
        List<TrackingEventInfo> events
) {
    public static DebugTrackingEventsResponse empty(
            String clientNo,
            String mobileNo,
            String requestedUserId,
            Long userId,
            String partnerUserId
    ) {
        return new DebugTrackingEventsResponse(
                false,
                clientNo,
                mobileNo,
                requestedUserId,
                userId,
                partnerUserId,
                0L,
                0,
                false,
                List.of(),
                List.of()
        );
    }

    public record EventTypeSummary(
            String eventType,
            long count
    ) {
    }

    public record TrackingEventInfo(
            long id,
            Long eventTimestamp,
            String eventDatetime,
            String eventType,
            String url,
            String uid,
            String traceId,
            String clientNo,
            String clientManufacture,
            String clientModel,
            String clientCategory,
            String clientOs,
            String clientOsVersion,
            String ai,
            String av,
            String wv,
            String bn,
            String bv,
            String androidId,
            String gaid,
            String idfv,
            String idfa,
            String ip,
            String extendJson,
            String payloadJson,
            String partnerUserId,
            Long userId,
            String source,
            Instant createdAt
    ) {
    }
}
