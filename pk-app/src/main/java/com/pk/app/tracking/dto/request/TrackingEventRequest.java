package com.pk.app.tracking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Flat client analytics payload aligned with Open Platform §26.
 */
public record TrackingEventRequest(
        @NotNull @Positive @JsonProperty("timestamp") Long timestamp,
        @Size(max = 128) @JsonProperty("uid") String uid,
        @NotBlank @Size(max = 128) @JsonProperty("eventType") String eventType,
        @NotBlank @Size(max = 512) @JsonProperty("url") String url,
        @JsonProperty("extend") Map<String, Object> extend,
        @NotBlank @Size(max = 64) @JsonProperty("traceId") String traceId,
        @NotBlank @Size(max = 128) @JsonProperty("clientNo") String clientNo,
        @NotBlank @Size(max = 64) @JsonProperty("clientManufacture") String clientManufacture,
        @NotBlank @Size(max = 128) @JsonProperty("clientModel") String clientModel,
        @NotBlank @Size(max = 32) @JsonProperty("clientCategory") String clientCategory,
        @NotBlank @Size(max = 32) @JsonProperty("clientOs") String clientOs,
        @NotBlank @Size(max = 64) @JsonProperty("clientOsVersion") String clientOsVersion,
        @NotBlank @Size(max = 128) @JsonProperty("ai") String ai,
        @NotBlank @Size(max = 64) @JsonProperty("av") String av,
        @Size(max = 64) @JsonProperty("wv") String wv,
        @Size(max = 64) @JsonProperty("bn") String bn,
        @Size(max = 64) @JsonProperty("bv") String bv,
        @Size(max = 128) @JsonProperty("androidId") String androidId,
        @Size(max = 128) @JsonProperty("gaid") String gaid,
        @Size(max = 128) @JsonProperty("idfv") String idfv,
        @Size(max = 128) @JsonProperty("idfa") String idfa
) {
}
