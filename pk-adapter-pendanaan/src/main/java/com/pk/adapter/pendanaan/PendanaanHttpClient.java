package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.external.port.LenderInteractionLogRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class PendanaanHttpClient {
    private final PendanaanProperties properties;
    private final PendanaanOAuthTokenProvider tokenProvider;
    private final LenderInteractionLogRepository interactionLogRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public PendanaanHttpClient(
            PendanaanProperties properties,
            PendanaanOAuthTokenProvider tokenProvider,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.tokenProvider = tokenProvider;
        this.interactionLogRepository = interactionLogRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .build();
    }

    public JsonNode get(String path, String businessType, String businessId) {
        return exchange("GET", path, null, businessType, businessId);
    }

    public JsonNode post(String path, String jsonBody, String businessType, String businessId) {
        return exchange("POST", path, jsonBody, businessType, businessId);
    }

    public JsonNode postEnvelope(String path, String jsonBody, String businessType, String businessId) {
        return exchangeEnvelope("POST", path, jsonBody, businessType, businessId);
    }

    private JsonNode exchange(
            String method,
            String path,
            String jsonBody,
            String businessType,
            String businessId
    ) {
        String endpoint = PendanaanHttpSupport.normalizeBaseUrl(properties.baseUrl())
                + PendanaanHttpSupport.normalizePath(path);
        String requestBody = jsonBody == null ? "" : jsonBody;
        String interactionNo = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        PendanaanHttpSupport.InteractionOutcome outcome = new PendanaanHttpSupport.InteractionOutcome();
        try {
            HttpResponse<String> response = send(method, endpoint, requestBody);
            outcome.httpStatus = response.statusCode();
            outcome.responseText = response.body() == null ? "" : response.body();
            JsonNode envelope = objectMapper.readTree(outcome.responseText);
            outcome.responseCode = PendanaanHttpSupport.textOrEmpty(envelope.get("code"));
            outcome.responseMsg = PendanaanHttpSupport.textOrEmpty(envelope.get("msg"));
            PendanaanHttpSupport.ensureSuccess(envelope);
            outcome.success = true;
            return envelope.get("data");
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            outcome.transportFailure = PendanaanHttpSupport.formatTransportFailure(exception);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            outcome.transportFailure = PendanaanHttpSupport.formatTransportFailure(exception);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } finally {
            logInteraction(
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
    }

    private JsonNode exchangeEnvelope(
            String method,
            String path,
            String jsonBody,
            String businessType,
            String businessId
    ) {
        String endpoint = PendanaanHttpSupport.normalizeBaseUrl(properties.baseUrl())
                + PendanaanHttpSupport.normalizePath(path);
        String requestBody = jsonBody == null ? "" : jsonBody;
        String interactionNo = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        PendanaanHttpSupport.InteractionOutcome outcome = new PendanaanHttpSupport.InteractionOutcome();
        try {
            HttpResponse<String> response = send(method, endpoint, requestBody);
            outcome.httpStatus = response.statusCode();
            outcome.responseText = response.body() == null ? "" : response.body();
            JsonNode envelope = objectMapper.readTree(outcome.responseText);
            outcome.responseCode = PendanaanHttpSupport.textOrEmpty(envelope.get("code"));
            outcome.responseMsg = PendanaanHttpSupport.textOrEmpty(envelope.get("msg"));
            outcome.success = ApiCode.SUCCESS.code().equals(outcome.responseCode);
            return envelope;
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            outcome.transportFailure = PendanaanHttpSupport.formatTransportFailure(exception);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            outcome.transportFailure = PendanaanHttpSupport.formatTransportFailure(exception);
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } finally {
            logInteraction(
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
    }

    private HttpResponse<String> send(String method, String endpoint, String requestBody)
            throws java.io.IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                .header("Authorization", "Bearer " + tokenProvider.getAccessToken());
        if ("POST".equals(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(requestBody));
        } else {
            builder.GET();
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void logInteraction(
            String interactionNo,
            String businessType,
            String businessId,
            String method,
            String endpoint,
            String requestBody,
            PendanaanHttpSupport.InteractionOutcome outcome,
            long startedAt
    ) {
        PendanaanHttpSupport.applyTransportFailureForLog(outcome);
        int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
        PendanaanInteractionSupport.log(
                interactionLogRepository,
                properties.logging(),
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
}
