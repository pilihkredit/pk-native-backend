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
                    clampCodeLength(intOrDefault(root, "codeLength", 6)),
                    parseUserList(root.get("userList"))
            );
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    /**
     * Aligns with pk-credit-core VerificationCodeService when {@code enableSms=false}
     * (everyone may use {@code defaultCode}). When SMS is enabled, only {@code userList}
     * mobiles may use {@code defaultCode}.
     */
    public static boolean acceptsConfiguredDefaultCode(SmsConf conf, String mobileNo, String otpCode) {
        if (conf == null || otpCode == null || otpCode.isBlank()) {
            return false;
        }
        String code = otpCode.trim();
        String defaultCode = conf.defaultCode() == null ? "" : conf.defaultCode().trim();
        if (defaultCode.isEmpty() || !defaultCode.equals(code)) {
            return false;
        }
        String mobile = mobileNo == null ? "" : mobileNo.trim();
        List<String> userList = conf.userList();
        boolean hasWhitelist = userList != null && !userList.isEmpty();
        if (hasWhitelist && userList.contains(mobile)) {
            return true;
        }
        // SMS disabled → mock mode: any mobile may use defaultCode (credit-core parity)
        if (!conf.enableSms()) {
            return true;
        }
        // SMS enabled + not on whitelist (or no whitelist) → reject defaultCode
        return false;
    }

    private static int clampCodeLength(int codeLength) {
        if (codeLength < 4) {
            return 4;
        }
        if (codeLength > 8) {
            return 8;
        }
        return codeLength;
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
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        // JSON string, or MySQL/driver quirks that surface as non-textual
        String value = node.isTextual() ? node.asText() : node.asText(null);
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
            return defaultValue;
        }
        return value.trim();
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
            int codeLength,
            List<String> userList
    ) {
        public SmsConf {
            userList = userList == null ? List.of() : List.copyOf(userList);
            codeLength = clampCodeLength(codeLength <= 0 ? 6 : codeLength);
        }
    }
}
