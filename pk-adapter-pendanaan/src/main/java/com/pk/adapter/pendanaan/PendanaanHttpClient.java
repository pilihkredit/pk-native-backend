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
        String responseText = "";
        String responseCode = "";
        String responseMsg = "";
        boolean success = false;
        try {
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
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            responseText = response.body() == null ? "" : response.body();
            JsonNode envelope = objectMapper.readTree(responseText);
            responseCode = PendanaanHttpSupport.textOrEmpty(envelope.get("code"));
            responseMsg = PendanaanHttpSupport.textOrEmpty(envelope.get("msg"));
            PendanaanHttpSupport.ensureSuccess(envelope);
            success = true;
            return envelope.get("data");
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } finally {
            int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
            PendanaanInteractionSupport.log(
                    interactionLogRepository,
                    interactionNo,
                    businessType,
                    businessId,
                    method,
                    endpoint,
                    requestBody,
                    responseCode,
                    responseMsg,
                    responseText,
                    success,
                    durationMs
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
        String responseText = "";
        String responseCode = "";
        String responseMsg = "";
        boolean success = false;
        try {
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
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            responseText = response.body() == null ? "" : response.body();
            JsonNode envelope = objectMapper.readTree(responseText);
            responseCode = PendanaanHttpSupport.textOrEmpty(envelope.get("code"));
            responseMsg = PendanaanHttpSupport.textOrEmpty(envelope.get("msg"));
            success = ApiCode.SUCCESS.code().equals(responseCode);
            return envelope;
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } finally {
            int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
            PendanaanInteractionSupport.log(
                    interactionLogRepository,
                    interactionNo,
                    businessType,
                    businessId,
                    method,
                    endpoint,
                    requestBody,
                    responseCode,
                    responseMsg,
                    responseText,
                    success,
                    durationMs
            );
        }
    }
}
