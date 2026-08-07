package com.pk.infra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.port.WhatsAppSender;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Chuanglan (InnoPaaS) WhatsApp API V3 sender.
 * Credentials and enable flag come from {@code app_config.whatsappConf}.
 */
@Component
public class ChuanglanWhatsAppSender implements WhatsAppSender {
    private static final Logger log = LoggerFactory.getLogger(ChuanglanWhatsAppSender.class);
    private static final String PROVIDER_CODE = "chuanglan";

    private final WhatsAppConfigLoader whatsAppConfigLoader;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ChuanglanWhatsAppSender(WhatsAppConfigLoader whatsAppConfigLoader, ObjectMapper objectMapper) {
        this.whatsAppConfigLoader = whatsAppConfigLoader;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public SmsSendResult send(String mobileNo, String otpCode) {
        WhatsAppConfigLoader.WhatsAppConf conf = whatsAppConfigLoader.loadConf();
        if (!conf.enableWhatsApp()) {
            log.info("WhatsApp disabled in whatsappConf; mock send for mobile {}", mobileNo);
            return SmsSendResult.success(PROVIDER_CODE, "mock-" + UUID.randomUUID());
        }
        try {
            String payload = objectMapper.writeValueAsString(buildPayload(conf, mobileNo, otpCode));
            String baseUrl = trimTrailingSlash(conf.url());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/msg/submit"))
                    .timeout(conf.timeout())
                    .header("Content-Type", "application/json;charset=utf-8")
                    .header("Authorization", nullToEmpty(conf.authorization()))
                    .header("AppKey", nullToEmpty(conf.appKey()))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("WhatsApp API status={} mobile={} body={}", response.statusCode(), mobileNo, response.body());
            return parseResponse(response.body(), mobileNo);
        } catch (Exception exception) {
            log.error("WhatsApp send failed mobile={}: {}", mobileNo, exception.getMessage(), exception);
            return SmsSendResult.failure(PROVIDER_CODE, "SEND_ERROR", exception.getMessage());
        }
    }

    static Map<String, Object> buildPayload(
            WhatsAppConfigLoader.WhatsAppConf conf,
            String mobileNo,
            String otpCode
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("wabaId", conf.wabaId());
        payload.put("messageType", "template");
        payload.put("bodyParams", List.of(otpCode));
        payload.put("recipientNumber", conf.countryDialCode() + mobileNo);
        payload.put("sendNumber", conf.sendNumber());
        payload.put("templateGroupId", conf.templateGroupId());
        payload.put("language", conf.language());

        Map<String, String> button = new HashMap<>();
        button.put("type", "text");
        button.put("value", otpCode);
        List<Map<String, String>> buttonParams = new ArrayList<>();
        buttonParams.add(button);
        payload.put("buttonParams", buttonParams);
        return payload;
    }

    private SmsSendResult parseResponse(String responseBody, String mobileNo) {
        try {
            JsonNode root = objectMapper.readTree(responseBody == null ? "{}" : responseBody);
            String code = text(root, "code");
            String message = text(root, "message");
            if ("0".equals(code)) {
                String messageId = root.has("data") && !root.get("data").isNull()
                        ? root.get("data").asText()
                        : "msg-" + System.currentTimeMillis();
                return SmsSendResult.success(PROVIDER_CODE, messageId);
            }
            log.warn("WhatsApp provider rejected mobile={} code={} message={}", mobileNo, code, message);
            return SmsSendResult.failure(PROVIDER_CODE, code, message);
        } catch (Exception exception) {
            return SmsSendResult.failure(PROVIDER_CODE, "PARSE_ERROR", exception.getMessage());
        }
    }

    private static String text(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return "";
        }
        return node.get(field).asText();
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
