package com.pk.infra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.time.Duration;
import java.time.ZoneId;

/**
 * Loads OTP send limits from {@code app_config} key {@value #CONFIG_KEY}.
 */
public class AuthOtpConfigLoader {
    public static final String CONFIG_KEY = "auth.otp";

    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public AuthOtpConfigLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public AuthOtpConfig load() {
        AppConfigRepository.AppConfigRecord record = appConfigRepository.findByKey(CONFIG_KEY)
                .orElseThrow(() -> new ApiException(ApiCode.SERVICE_UNAVAILABLE, "auth.otp config missing"));
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            int dailyLimit = requirePositiveInt(root, "otpDailyLimit");
            int resendSeconds = requirePositiveInt(root, "otpResendIntervalSeconds");
            String zone = requireText(root, "otpDailyLimitZone");
            return new AuthOtpConfig(dailyLimit, Duration.ofSeconds(resendSeconds), ZoneId.of(zone));
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private static int requirePositiveInt(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || !node.isNumber() || node.asInt() <= 0) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, "auth.otp." + field + " invalid");
        }
        return node.asInt();
    }

    private static String requireText(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull() || node.asText().isBlank()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, "auth.otp." + field + " invalid");
        }
        return node.asText().trim();
    }

    public record AuthOtpConfig(
            int otpDailyLimit,
            Duration otpResendInterval,
            ZoneId otpDailyLimitZone
    ) {
    }
}
