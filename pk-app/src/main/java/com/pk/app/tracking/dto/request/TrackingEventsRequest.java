package com.pk.app.tracking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

public record TrackingEventsRequest(
        @NotEmpty @Size(max = 50) @Valid List<TrackingEventItem> events
) {
    public record TrackingEventItem(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("eventTime") Long eventTime,
            @JsonProperty("traceId") String traceId,
            @JsonProperty("url") String url,
            @JsonProperty("extend") Map<String, Object> extend,
            @JsonProperty("clientNo") String clientNo,
            @JsonProperty("clientManufacture") String clientManufacture,
            @JsonProperty("clientModel") String clientModel,
            @JsonProperty("clientCategory") String clientCategory,
            @JsonProperty("clientOs") String clientOs,
            @JsonProperty("clientOsVersion") String clientOsVersion,
            @JsonProperty("ai") String ai,
            @JsonProperty("av") String av,
            @JsonProperty("wv") String wv,
            @JsonProperty("bn") String bn,
            @JsonProperty("bv") String bv,
            @JsonProperty("androidId") String androidId,
            @JsonProperty("gaid") String gaid,
            @JsonProperty("idfv") String idfv,
            @JsonProperty("idfa") String idfa
    ) {
    }
}
