package com.pk.core.tracking.port;

public interface TrackingEventRepository {
    void insert(TrackingEventInsert event);

    record TrackingEventInsert(
            Long eventTimestamp,
            String uid,
            String eventType,
            String url,
            String extendJson,
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
            String eventDatetime,
            String payloadJson,
            String partnerUserId,
            Long userId,
            String source
    ) {
    }
}
