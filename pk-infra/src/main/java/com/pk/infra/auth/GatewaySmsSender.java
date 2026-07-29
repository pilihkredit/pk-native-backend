package com.pk.infra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.port.SmsSender;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SMS OTP sender aligned with pk-credit-core {@code SmsService}:
 * GET gateway with spid/pwd/sm/timestamp/das, Indonesian PilihKredit template.
 */
@Component
public class GatewaySmsSender implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(GatewaySmsSender.class);
    private static final String PROVIDER_CODE = "sms-gateway";

    private final SmsConfigLoader smsConfigLoader;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GatewaySmsSender(SmsConfigLoader smsConfigLoader, ObjectMapper objectMapper) {
        this.smsConfigLoader = smsConfigLoader;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public SmsSendResult send(String mobileNo, String otpCode) {
        SmsConfigLoader.SmsConf conf = smsConfigLoader.loadConf();
        String content = buildSmsContent(conf.contentTemplate(), otpCode, conf.expireTimeSeconds());
        if (!conf.enableSms()) {
            log.info("SMS disabled in smsConf; mock send for mobile {}", mobileNo);
            return SmsSendResult.success(PROVIDER_CODE, "mock-" + UUID.randomUUID());
        }
        if (isBlank(conf.url()) || isBlank(conf.spid()) || isBlank(conf.pwd())) {
            log.error("SMS gateway config incomplete (url/spid/pwd) mobile={}", mobileNo);
            return SmsSendResult.failure(PROVIDER_CODE, "CONFIG_MISSING", "smsConf url/spid/pwd required");
        }
        try {
            long timestamp = System.currentTimeMillis() / 1000;
            Map<String, String> params = new LinkedHashMap<>();
            params.put("spid", conf.spid());
            params.put("pwd", generatePassword(timestamp, conf.spid(), conf.pwd()));
            params.put("sm", stringToHex(content));
            params.put("timestamp", Long.toString(timestamp));
            params.put("das", conf.commercialCode() + normalizeMobile(mobileNo));

            URI uri = URI.create(appendQuery(conf.url().trim(), params));
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(conf.timeoutMs()))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("SMS gateway status={} mobile={} body={}", response.statusCode(), mobileNo, response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return SmsSendResult.failure(PROVIDER_CODE, "HTTP_" + response.statusCode(), response.body());
            }
            return parseResponse(response.body());
        } catch (Exception exception) {
            log.error("SMS send failed mobile={}: {}", mobileNo, exception.getMessage(), exception);
            return SmsSendResult.failure(PROVIDER_CODE, "SEND_ERROR", exception.getMessage());
        }
    }

    static String buildSmsContent(String template, String otpCode, int expireTimeSeconds) {
        String content = template == null || template.isBlank()
                ? SmsConfigLoader.DEFAULT_CONTENT_TEMPLATE
                : template;
        int expireMinutes = Math.max(1, expireTimeSeconds / 60);
        return content
                .replace("{code}", otpCode == null ? "" : otpCode)
                .replace("{minutes}", Integer.toString(expireMinutes))
                .replace("{seconds}", Integer.toString(expireTimeSeconds));
    }

    private SmsSendResult parseResponse(String responseBody) {
        try {
            if (responseBody == null || responseBody.isBlank()) {
                return SmsSendResult.failure(PROVIDER_CODE, "EMPTY_RESPONSE", "empty gateway response");
            }
            JsonNode root = objectMapper.readTree(responseBody);
            int code = root.path("code").asInt(-1);
            if (code == 0) {
                String msgid = root.path("data").path("msgid").asText(null);
                if (msgid == null || msgid.isBlank()) {
                    msgid = PROVIDER_CODE + "-" + UUID.randomUUID();
                }
                return SmsSendResult.success(PROVIDER_CODE, msgid);
            }
            String errorMsg = root.path("msg").asText("Unknown error");
            return SmsSendResult.failure(PROVIDER_CODE, "GATEWAY_" + code, errorMsg);
        } catch (Exception exception) {
            return SmsSendResult.failure(PROVIDER_CODE, "PARSE_ERROR", exception.getMessage());
        }
    }

    private static String generatePassword(long timestamp, String spid, String pwd) {
        try {
            String input = spid + "00000000" + pwd + timestamp;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("MD5 not available", exception);
        }
    }

    /** Same as pk-credit-core SmsService.stringToHex (UTF-16 code unit as int). */
    static String stringToHex(String input) {
        StringBuilder hex = new StringBuilder();
        for (char c : input.toCharArray()) {
            hex.append(String.format("%02x", (int) c));
        }
        return hex.toString();
    }

    private static String appendQuery(String baseUrl, Map<String, String> params) {
        StringBuilder builder = new StringBuilder(baseUrl);
        builder.append(baseUrl.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                builder.append('&');
            }
            first = false;
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue() == null ? "" : entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private static String normalizeMobile(String mobileNo) {
        if (mobileNo == null) {
            return "";
        }
        String normalized = mobileNo.trim();
        while (normalized.startsWith("0")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
