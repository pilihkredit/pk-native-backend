package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.logging.PlatformStructuredLogger;
import com.pk.core.tracking.LenderTrackingEvent;
import com.pk.core.tracking.port.LenderTrackingPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

public class ApiPartnerTrackingAdapter implements LenderTrackingPort {
    static final String BUSINESS_TYPE = "TRACKING_EVENT";

    private final ApiPartnerProperties properties;
    private final LenderInteractionLogRepository interactionLogRepository;
    private final ObjectMapper objectMapper;
    private final PlatformStructuredLogger structuredLogger;
    private final HttpClient httpClient;

    public ApiPartnerTrackingAdapter(
            ApiPartnerProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper,
            PlatformStructuredLogger structuredLogger
    ) {
        this.properties = properties;
        this.interactionLogRepository = interactionLogRepository;
        this.objectMapper = objectMapper;
        this.structuredLogger = structuredLogger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .build();
    }

    @Override
    public void submitEvents(Collection<LenderTrackingEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (LenderTrackingEvent event : events) {
            submitEvent(event);
        }
    }

    private void submitEvent(LenderTrackingEvent event) {
        String endpoint = properties.trackingUrl();
        String requestBody = toRequestBody(event);
        String interactionNo = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        ApiPartnerHttpSupport.InteractionOutcome outcome = new ApiPartnerHttpSupport.InteractionOutcome();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            outcome.httpStatus = response.statusCode();
            outcome.responseText = response.body() == null ? "" : response.body();
            outcome.responseCode = "HTTP_" + response.statusCode();
            outcome.responseMsg = outcome.responseText;
            outcome.success = response.statusCode() >= 200 && response.statusCode() < 300;
            if (!outcome.success) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            outcome.transportFailure = ApiPartnerHttpSupport.formatTransportFailure(exception);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            outcome.transportFailure = ApiPartnerHttpSupport.formatTransportFailure(exception);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } finally {
            logInteraction(interactionNo, event.eventType(), endpoint, requestBody, outcome, startedAt);
        }
    }

    private String toRequestBody(LenderTrackingEvent event) {
        ObjectNode node = objectMapper.createObjectNode();
        put(node, "timestamp", event.timestamp());
        put(node, "uid", event.uid());
        put(node, "eventType", event.eventType());
        put(node, "url", event.url());
        if (event.extend() != null && !event.extend().isEmpty()) {
            node.set("extend", objectMapper.valueToTree(event.extend()));
        }
        put(node, "traceId", event.traceId());
        put(node, "clientNo", event.clientNo());
        put(node, "clientManufacture", event.clientManufacture());
        put(node, "clientModel", event.clientModel());
        put(node, "clientCategory", event.clientCategory());
        put(node, "clientOs", event.clientOs());
        put(node, "clientOsVersion", event.clientOsVersion());
        put(node, "ai", event.ai());
        put(node, "av", event.av());
        put(node, "wv", event.wv());
        put(node, "bn", event.bn());
        put(node, "bv", event.bv());
        put(node, "androidId", event.androidId());
        put(node, "gaid", event.gaid());
        put(node, "idfv", event.idfv());
        put(node, "idfa", event.idfa());
        put(node, "ip", event.ip());
        put(node, "datetime", event.datetime());
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private static void put(ObjectNode node, String field, String value) {
        if (value != null) {
            node.put(field, value);
        }
    }

    private static void put(ObjectNode node, String field, Long value) {
        if (value != null) {
            node.put(field, value);
        }
    }

    private void logInteraction(
            String interactionNo,
            String businessId,
            String endpoint,
            String requestBody,
            ApiPartnerHttpSupport.InteractionOutcome outcome,
            long startedAt
    ) {
        ApiPartnerHttpSupport.applyTransportFailureForLog(outcome);
        int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
        ApiPartnerInteractionSupport.log(
                interactionLogRepository,
                properties.logging(),
                structuredLogger,
                interactionNo,
                BUSINESS_TYPE,
                businessId,
                "POST",
                endpoint,
                requestBody,
                outcome.responseCode,
                outcome.responseMsg,
                outcome.responseText,
                outcome.success,
                durationMs,
                outcome.httpStatus
        );
    }
}
