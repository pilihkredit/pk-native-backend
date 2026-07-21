package com.pk.infra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.time.Duration;

/**
 * Loads WhatsApp provider settings and daily send limit from {@code app_config}.
 *
 * <ul>
 *   <li>{@value #CONF_KEY} — Chuanglan WhatsApp API V3 settings (JSON object)</li>
 *   <li>{@value #DAILY_LIMIT_KEY} — max WhatsApp OTP sends per mobile per day (JSON number)</li>
 * </ul>
 */
public class WhatsAppConfigLoader {
    public static final String CONF_KEY = "whatsappConf";
    public static final String DAILY_LIMIT_KEY = "whatsapp_daily_limit";
    private static final int DEFAULT_DAILY_LIMIT = 5;

    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public WhatsAppConfigLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public WhatsAppConf loadConf() {
        AppConfigRepository.AppConfigRecord record = appConfigRepository.findByKey(CONF_KEY)
                .orElseThrow(() -> new ApiException(ApiCode.SERVICE_UNAVAILABLE, "whatsappConf config missing"));
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            if (root == null || !root.isObject()) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, "whatsappConf invalid");
            }
            return new WhatsAppConf(
                    booleanOrDefault(root, "enableWhatsApp", false),
                    textOrDefault(root, "url", "https://api.innopaas.com/api/whatsapp/v3"),
                    textOrDefault(root, "appKey", ""),
                    textOrDefault(root, "authorization", ""),
                    textOrDefault(root, "wabaId", ""),
                    textOrDefault(root, "sendNumber", ""),
                    textOrDefault(root, "templateName", "otp_pilihkredit"),
                    textOrDefault(root, "language", "id"),
                    textOrDefault(root, "countryDialCode", "62"),
                    Duration.ofMillis(Math.max(1_000, intOrDefault(root, "timeout", 10_000))),
                    Duration.ofSeconds(Math.max(1, intOrDefault(root, "minInterval", 60))),
                    Duration.ofSeconds(Math.max(1, intOrDefault(root, "expireTime", 300)))
            );
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    /**
     * Daily WhatsApp send limit per mobile. Falls back to {@value #DEFAULT_DAILY_LIMIT} when missing/invalid
     * (same default as pk-credit-core {@code @DailyRequestLimit}).
     */
    public int loadDailyLimit() {
        return appConfigRepository.findByKey(DAILY_LIMIT_KEY)
                .map(this::parseDailyLimit)
                .orElse(DEFAULT_DAILY_LIMIT);
    }

    private int parseDailyLimit(AppConfigRepository.AppConfigRecord record) {
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            int limit;
            if (root != null && root.isNumber()) {
                limit = root.asInt();
            } else if (root != null && root.isTextual()) {
                limit = Integer.parseInt(root.asText().trim());
            } else if (root != null && root.isObject() && root.has("limit") && root.get("limit").isNumber()) {
                limit = root.get("limit").asInt();
            } else {
                return DEFAULT_DAILY_LIMIT;
            }
            return limit > 0 ? limit : DEFAULT_DAILY_LIMIT;
        } catch (Exception exception) {
            return DEFAULT_DAILY_LIMIT;
        }
    }

    private static boolean booleanOrDefault(JsonNode root, String field, boolean defaultValue) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isTextual()) {
            return Boolean.parseBoolean(node.asText().trim());
        }
        return defaultValue;
    }

    private static int intOrDefault(JsonNode root, String field, int defaultValue) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull() || !node.isNumber()) {
            return defaultValue;
        }
        return node.asInt();
    }

    private static String textOrDefault(JsonNode root, String field, String defaultValue) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull() || node.asText().isBlank()) {
            return defaultValue;
        }
        return node.asText().trim();
    }

    public record WhatsAppConf(
            boolean enableWhatsApp,
            String url,
            String appKey,
            String authorization,
            String wabaId,
            String sendNumber,
            String templateName,
            String language,
            String countryDialCode,
            Duration timeout,
            Duration minInterval,
            Duration expireTime
    ) {
    }
}
