package com.pk.infra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.time.Duration;

/**
 * Loads device-switch security knobs from {@code app_config} key {@value #CONFIG_KEY}; every
 * send/verify reads fresh so DB changes apply without restart.
 *
 * <pre>{@code
 * {
 *   "livenessLicenseMaxPerIp": 100,
 *   "livenessLicenseMaxPerDevice": 5,
 *   "livenessLicenseWindowMinutes": 10,
 *   "livenessLicenseBlockMinutes": 15,
 *   "faceVerifyMaxFailures": 5,
 *   "faceVerifyFailWindowMinutes": 60,
 *   "livenessSessionDurationSeconds": 600,
 *   "deviceNoMaxLength": 128
 * }
 * }</pre>
 *
 * <p>Missing key or invalid fields fall back to defaults; deviceNoMaxLength is capped at 128
 * because device_no columns are VARCHAR(128).
 */
public class DeviceSwitchSecurityConfigLoader {
    public static final String CONFIG_KEY = "device_switch_security";

    private static final int DEFAULT_LICENSE_MAX_PER_IP = 100;
    private static final int DEFAULT_LICENSE_MAX_PER_DEVICE = 5;
    private static final int DEFAULT_LICENSE_WINDOW_MINUTES = 10;
    private static final int DEFAULT_LICENSE_BLOCK_MINUTES = 15;
    private static final int DEFAULT_FACE_VERIFY_MAX_FAILURES = 5;
    private static final int DEFAULT_FACE_VERIFY_FAIL_WINDOW_MINUTES = 60;
    private static final int DEFAULT_LIVENESS_SESSION_SECONDS = 600;
    private static final int DEFAULT_DEVICE_NO_MAX_LENGTH = 128;
    private static final int DEVICE_NO_COLUMN_MAX = 128;

    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public DeviceSwitchSecurityConfigLoader(
            AppConfigRepository appConfigRepository,
            ObjectMapper objectMapper
    ) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public Settings loadSettings() {
        return appConfigRepository.findByKey(CONFIG_KEY)
                .map(this::parseSettings)
                .orElse(Settings.defaults());
    }

    private Settings parseSettings(AppConfigRepository.AppConfigRecord record) {
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            if (root == null || root.isNull() || !root.isObject()) {
                return Settings.defaults();
            }
            return new Settings(
                    intInRange(root, "livenessLicenseMaxPerIp", 1, 100_000, DEFAULT_LICENSE_MAX_PER_IP),
                    intInRange(root, "livenessLicenseMaxPerDevice", 1, 100_000,
                            DEFAULT_LICENSE_MAX_PER_DEVICE),
                    Duration.ofMinutes(intInRange(root, "livenessLicenseWindowMinutes", 1, 24 * 60,
                            DEFAULT_LICENSE_WINDOW_MINUTES)),
                    Duration.ofMinutes(intInRange(root, "livenessLicenseBlockMinutes", 1, 24 * 60,
                            DEFAULT_LICENSE_BLOCK_MINUTES)),
                    intInRange(root, "faceVerifyMaxFailures", 1, 1_000,
                            DEFAULT_FACE_VERIFY_MAX_FAILURES),
                    Duration.ofMinutes(intInRange(root, "faceVerifyFailWindowMinutes", 1, 24 * 60,
                            DEFAULT_FACE_VERIFY_FAIL_WINDOW_MINUTES)),
                    intInRange(root, "livenessSessionDurationSeconds", 60, 86_400,
                            DEFAULT_LIVENESS_SESSION_SECONDS),
                    intInRange(root, "deviceNoMaxLength", 1, DEVICE_NO_COLUMN_MAX,
                            DEFAULT_DEVICE_NO_MAX_LENGTH)
            );
        } catch (Exception exception) {
            return Settings.defaults();
        }
    }

    private static int intInRange(JsonNode root, String field, int min, int max, int defaultValue) {
        JsonNode node = root.get(field);
        if (node == null || !node.canConvertToInt()) {
            return defaultValue;
        }
        int value = node.asInt();
        if (value < min || value > max) {
            return defaultValue;
        }
        return value;
    }

    public record Settings(
            int livenessLicenseMaxPerIp,
            int livenessLicenseMaxPerDevice,
            Duration livenessLicenseWindow,
            Duration livenessLicenseBlock,
            int faceVerifyMaxFailures,
            Duration faceVerifyFailWindow,
            int livenessSessionDurationSeconds,
            int deviceNoMaxLength
    ) {
        static Settings defaults() {
            return new Settings(
                    DEFAULT_LICENSE_MAX_PER_IP,
                    DEFAULT_LICENSE_MAX_PER_DEVICE,
                    Duration.ofMinutes(DEFAULT_LICENSE_WINDOW_MINUTES),
                    Duration.ofMinutes(DEFAULT_LICENSE_BLOCK_MINUTES),
                    DEFAULT_FACE_VERIFY_MAX_FAILURES,
                    Duration.ofMinutes(DEFAULT_FACE_VERIFY_FAIL_WINDOW_MINUTES),
                    DEFAULT_LIVENESS_SESSION_SECONDS,
                    DEFAULT_DEVICE_NO_MAX_LENGTH
            );
        }
    }
}
