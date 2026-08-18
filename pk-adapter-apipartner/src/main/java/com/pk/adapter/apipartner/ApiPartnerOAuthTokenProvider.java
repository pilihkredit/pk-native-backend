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
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ApiPartnerOAuthTokenProvider {
    private static final String TOKEN_PATH = ApiPartnerOpenApiPaths.OAUTH_TOKEN;
    private static final long EXPIRY_SKEW_SECONDS = 60;

    private final ApiPartnerProperties properties;
    private final LenderInteractionLogRepository interactionLogRepository;
    private final ObjectMapper objectMapper;
    private final PlatformStructuredLogger structuredLogger;
    private final HttpClient httpClient;
    private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();

    public ApiPartnerOAuthTokenProvider(
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

    public String getAccessToken() {
        CachedToken current = cachedToken.get();
        if (current != null && current.isValid()) {
            return current.accessToken();
        }
        synchronized (this) {
            current = cachedToken.get();
            if (current != null && current.isValid()) {
                return current.accessToken();
            }
            CachedToken refreshed = requestToken();
            cachedToken.set(refreshed);
            return refreshed.accessToken();
        }
    }

    private CachedToken requestToken() {
        String endpoint = ApiPartnerHttpSupport.normalizeBaseUrl(properties.baseUrl()) + TOKEN_PATH;
        String interactionNo = UUID.randomUUID().toString();
        long startedAt = System.currentTimeMillis();
        String requestBody = "";
        String responseText = "";
        String responseCode = "";
        String responseMsg = "";
        boolean success = false;
        try {
            requestBody = objectMapper.writeValueAsString(new OAuthTokenRequest(
                    properties.clientId(),
                    properties.clientSecret(),
                    "client_credentials"
            ));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                    .header("Accept-Language", ApiPartnerHttpSupport.ACCEPT_LANGUAGE)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            responseText = response.body() == null ? "" : response.body();
            JsonNode envelope = objectMapper.readTree(responseText);
            responseCode = ApiPartnerHttpSupport.textOrEmpty(envelope.get("code"));
            responseMsg = ApiPartnerHttpSupport.textOrEmpty(envelope.get("msg"));
            ApiPartnerHttpSupport.ensureSuccess(envelope);
            JsonNode data = envelope.get("data");
            String accessToken = data.path("accessToken").asText("");
            int expiresIn = data.path("expiresIn").asInt(0);
            if (accessToken.isBlank() || expiresIn <= 0) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
            }
            success = true;
            return new CachedToken(accessToken, Instant.now().plusSeconds(expiresIn));
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        } finally {
            int durationMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startedAt);
            ApiPartnerInteractionSupport.log(
                    interactionLogRepository,
                    properties.logging(),
                    structuredLogger,
                    interactionNo,
                    "OAUTH_TOKEN",
                    null,
                    "POST",
                    endpoint,
                    requestBody,
                    responseCode,
                    responseMsg,
                    responseText,
                    success,
                    durationMs,
                    null
            );
        }
    }

    private record OAuthTokenRequest(String clientId, String clientSecret, String grantType) {
    }

    private record CachedToken(String accessToken, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt.minusSeconds(EXPIRY_SKEW_SECONDS));
        }
    }
}
