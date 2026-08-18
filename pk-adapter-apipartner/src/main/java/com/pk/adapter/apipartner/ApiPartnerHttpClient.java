package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.logging.PlatformStructuredLogger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class ApiPartnerHttpClient {
    private final ApiPartnerProperties properties;
    private final ApiPartnerOAuthTokenProvider tokenProvider;
    private final LenderInteractionLogRepository interactionLogRepository;
    private final ObjectMapper objectMapper;
    private final PlatformStructuredLogger structuredLogger;
    private final HttpClient httpClient;

    public ApiPartnerHttpClient(
            ApiPartnerProperties properties,
            ApiPartnerOAuthTokenProvider tokenProvider,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper,
            PlatformStructuredLogger structuredLogger
    ) {
        this.properties = properties;
        this.tokenProvider = tokenProvider;
        this.interactionLogRepository = interactionLogRepository;
        this.objectMapper = objectMapper;
        this.structuredLogger = structuredLogger;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .build();
    }

    public JsonNode get(String path, String businessType, String businessId) {
        return exchange("GET", path, null, businessType, businessId).data();
    }

    public JsonNode post(String path, String jsonBody, String businessType, String businessId) {
        return exchange("POST", path, jsonBody, businessType, businessId).data();
    }

    public ExchangeResult postWithInteraction(String path, String jsonBody, String businessType, String businessId) {
        return exchange("POST", path, jsonBody, businessType, businessId);
    }

    public JsonNode postEnvelope(String path, String jsonBody, String businessType, String businessId) {
        return postEnvelopeWithInteraction(path, jsonBody, businessType, businessId).envelope();
    }

    public EnvelopeResult postEnvelopeWithInteraction(
            String path,
            String jsonBody,
            String businessType,
            String businessId
    ) {
        return exchangeEnvelope("POST", path, jsonBody, businessType, businessId);
    }

    private ExchangeResult exchange(
            String method,
            String path,
            String jsonBody,
            String businessType,
            String businessId
    ) {
        String endpoint = ApiPartnerHttpSupport.normalizeBaseUrl(properties.baseUrl())
                + ApiPartnerHttpSupport.normalizePath(path);
        String requestBody = jsonBody == null ? "" : jsonBody;
        String interactionNo = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        ApiPartnerHttpSupport.InteractionOutcome outcome = new ApiPartnerHttpSupport.InteractionOutcome();
        JsonNode data = null;
        ApiException failure = null;
        try {
            HttpResponse<String> response = send(method, endpoint, requestBody);
            outcome.httpStatus = response.statusCode();
            outcome.responseText = response.body() == null ? "" : response.body();
            JsonNode envelope = objectMapper.readTree(outcome.responseText);
            outcome.responseCode = ApiPartnerHttpSupport.textOrEmpty(envelope.get("code"));
            outcome.responseMsg = ApiPartnerHttpSupport.textOrEmpty(envelope.get("msg"));
            ApiPartnerHttpSupport.ensureSuccess(envelope);
            outcome.success = true;
            data = envelope.get("data");
        } catch (ApiException exception) {
            failure = exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            outcome.transportFailure = ApiPartnerHttpSupport.formatTransportFailure(exception);
            failure = new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            outcome.transportFailure = ApiPartnerHttpSupport.formatTransportFailure(exception);
            failure = new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } finally {
            outcome.interactionId = logInteraction(
                    interactionNo,
                    businessType,
                    businessId,
                    method,
                    endpoint,
                    requestBody,
                    outcome,
                    startedAt
            );
        }
        if (failure != null) {
            throw failure;
        }
        return new ExchangeResult(data, outcome.interactionId);
    }

    private EnvelopeResult exchangeEnvelope(
            String method,
            String path,
            String jsonBody,
            String businessType,
            String businessId
    ) {
        String endpoint = ApiPartnerHttpSupport.normalizeBaseUrl(properties.baseUrl())
                + ApiPartnerHttpSupport.normalizePath(path);
        String requestBody = jsonBody == null ? "" : jsonBody;
        String interactionNo = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        ApiPartnerHttpSupport.InteractionOutcome outcome = new ApiPartnerHttpSupport.InteractionOutcome();
        JsonNode envelope = null;
        ApiException failure = null;
        try {
            HttpResponse<String> response = send(method, endpoint, requestBody);
            outcome.httpStatus = response.statusCode();
            outcome.responseText = response.body() == null ? "" : response.body();
            envelope = objectMapper.readTree(outcome.responseText);
            outcome.responseCode = ApiPartnerHttpSupport.textOrEmpty(envelope.get("code"));
            outcome.responseMsg = ApiPartnerHttpSupport.textOrEmpty(envelope.get("msg"));
            outcome.success = ApiCode.SUCCESS.code().equals(outcome.responseCode);
        } catch (ApiException exception) {
            failure = exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            outcome.transportFailure = ApiPartnerHttpSupport.formatTransportFailure(exception);
            failure = new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            outcome.transportFailure = ApiPartnerHttpSupport.formatTransportFailure(exception);
            failure = new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } finally {
            outcome.interactionId = logInteraction(
                    interactionNo,
                    businessType,
                    businessId,
                    method,
                    endpoint,
                    requestBody,
                    outcome,
                    startedAt
            );
        }
        if (failure != null) {
            throw failure;
        }
        return new EnvelopeResult(envelope, outcome.interactionId);
    }

    private HttpResponse<String> send(String method, String endpoint, String requestBody)
            throws java.io.IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                .header("Accept-Language", ApiPartnerHttpSupport.ACCEPT_LANGUAGE)
                .header("Authorization", "Bearer " + tokenProvider.getAccessToken());
        if ("POST".equals(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
        } else {
            builder.GET();
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private long logInteraction(
            String interactionNo,
            String businessType,
            String businessId,
            String method,
            String endpoint,
            String requestBody,
            ApiPartnerHttpSupport.InteractionOutcome outcome,
            long startedAt
    ) {
        ApiPartnerHttpSupport.applyTransportFailureForLog(outcome);
        int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
        return ApiPartnerInteractionSupport.log(
                interactionLogRepository,
                properties.logging(),
                structuredLogger,
                interactionNo,
                businessType,
                businessId,
                method,
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

    public record ExchangeResult(JsonNode data, Long interactionId) {
    }

    public record EnvelopeResult(JsonNode envelope, Long interactionId) {
    }
}
