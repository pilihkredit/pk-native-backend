package com.pk.infra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads SMS gateway settings from {@code app_config.smsConf} (same shape as pk-credit-core).
 */
public class SmsConfigLoader {
    public static final String CONF_KEY = "smsConf";
    public static final String DEFAULT_CONTENT_TEMPLATE =
            "[PilihKredit] Kode verifikasi Anda adalah {code} valid selama {minutes} menit. "
                    + "JANGAN Bagikan kode ini kepada siapapun!";

    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public SmsConfigLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public SmsConf loadConf() {
        AppConfigRepository.AppConfigRecord record = appConfigRepository.findByKey(CONF_KEY)
                .orElseThrow(() -> new ApiException(ApiCode.SERVICE_UNAVAILABLE, "smsConf config missing"));
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            if (root == null || !root.isObject()) {
                throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, "smsConf invalid");
            }
            return new SmsConf(
                    booleanOrDefault(root, "enableSms", true),
                    textOrDefault(root, "url", ""),
                    textOrDefault(root, "spid", ""),
                    textOrDefault(root, "pwd", ""),
                    textOrDefault(root, "commercialCode", "0062"),
                    textOrDefault(root, "contentTemplate", DEFAULT_CONTENT_TEMPLATE),
                    Math.max(60, intOrDefault(root, "expireTime", 300)),
                    Math.max(1_000, intOrDefault(root, "timeout", 10_000)),
                    textOrDefault(root, "defaultCode", "1234"),
                    parseUserList(root.get("userList"))
            );
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    /**
     * Credit-core parity: whitelist + defaultCode, or defaultCode when SMS is disabled.
     */
    static boolean acceptsConfiguredDefaultCode(SmsConf conf, String mobileNo, String otpCode) {
        if (conf == null || otpCode == null || otpCode.isBlank()) {
            return false;
        }
        String code = otpCode.trim();
        String defaultCode = conf.defaultCode() == null ? "" : conf.defaultCode().trim();
        if (defaultCode.isEmpty() || !defaultCode.equals(code)) {
            return false;
        }
        if (conf.userList() != null && !conf.userList().isEmpty()) {
            String mobile = mobileNo == null ? "" : mobileNo.trim();
            if (conf.userList().contains(mobile)) {
                return true;
            }
        }
        return !conf.enableSms();
    }

    private List<String> parseUserList(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (node.isArray()) {
            List<String> users = new ArrayList<>();
            for (JsonNode item : node) {
                if (item == null || item.isNull()) {
                    continue;
                }
                String value = item.asText(null);
                if (value != null && !value.isBlank()) {
                    users.add(value.trim());
                }
            }
            return Collections.unmodifiableList(users);
        }
        if (node.isTextual()) {
            String text = node.asText().trim();
            if (text.isEmpty() || !text.startsWith("[")) {
                return List.of();
            }
            try {
                return parseUserList(objectMapper.readTree(text));
            } catch (Exception ignored) {
                return List.of();
            }
        }
        return List.of();
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
        if (node.isNumber()) {
            return node.asInt() != 0;
        }
        return defaultValue;
    }

    private static String textOrDefault(JsonNode root, String field, String defaultValue) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull() || !node.isTextual()) {
            if (node != null && node.isNumber()) {
                return node.asText();
            }
            return defaultValue;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static int intOrDefault(JsonNode root, String field, int defaultValue) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        if (node.isNumber()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public record SmsConf(
            boolean enableSms,
            String url,
            String spid,
            String pwd,
            String commercialCode,
            String contentTemplate,
            int expireTimeSeconds,
            int timeoutMs,
            String defaultCode,
            List<String> userList
    ) {
        public SmsConf {
            userList = userList == null ? List.of() : List.copyOf(userList);
        }
    }
}
