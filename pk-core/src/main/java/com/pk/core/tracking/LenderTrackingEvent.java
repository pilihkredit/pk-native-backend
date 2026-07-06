package com.pk.core.tracking;

import java.util.Map;

public record LenderTrackingEvent(
        Long timestamp,
        String uid,
        String eventType,
        String url,
        Map<String, Object> extend,
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
        String datetime
) {
}
