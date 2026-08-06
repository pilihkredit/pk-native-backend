package com.pk.infra.push;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.port.FcmPushPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class GoogleFcmPushAdapter implements FcmPushPort {
    private final FcmProperties properties;
    private final GoogleServiceAccountTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GoogleFcmPushAdapter(
            FcmProperties properties,
            GoogleServiceAccountTokenProvider tokenProvider,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public FcmSendResult send(FcmSendCommand command) {
        if (command == null || command.fcmToken() == null || command.fcmToken().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        try {
            String url = "https://fcm.googleapis.com/v1/projects/"
                    + properties.projectId().trim()
                    + "/messages:send";
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode message = root.putObject("message");
            message.put("token", command.fcmToken().trim());
            if ((command.title() != null && !command.title().isBlank())
                    || (command.body() != null && !command.body().isBlank())) {
                ObjectNode notification = message.putObject("notification");
                if (command.title() != null && !command.title().isBlank()) {
                    notification.put("title", command.title().trim());
                }
                if (command.body() != null && !command.body().isBlank()) {
                    notification.put("body", command.body().trim());
                }
            }
            Map<String, String> data = command.data();
            if (data != null && !data.isEmpty()) {
                ObjectNode dataNode = message.putObject("data");
                data.forEach((key, value) -> {
                    if (key != null && !key.isBlank() && value != null) {
                        dataNode.put(key, value);
                    }
                });
            }
            String payload = objectMapper.writeValueAsString(root);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + tokenProvider.accessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body() == null ? "" : response.body();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new FcmSendResult(false, null, body.isBlank()
                        ? ("HTTP " + response.statusCode())
                        : body);
            }
            JsonNode json = objectMapper.readTree(body);
            String name = json.path("name").asText(null);
            return new FcmSendResult(true, name, body);
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
