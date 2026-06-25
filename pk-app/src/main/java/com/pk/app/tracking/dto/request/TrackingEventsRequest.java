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
            @JsonProperty("extend") Map<String, Object> extend
    ) {
    }
}
