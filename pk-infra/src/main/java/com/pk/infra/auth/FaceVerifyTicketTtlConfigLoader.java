package com.pk.infra.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import java.time.Duration;

/**
 * Loads face-verify ticket TTL from {@code app_config} key {@value #CONFIG_KEY}.
 *
 * <p>JSON object, minutes (optional per-scene overrides fall back to {@code ttlMinutes}):
 *
 * <pre>{@code
 * {
 *   "ttlMinutes": 5,
 *   "deviceSwitchLoginMinutes": 5,
 *   "bankCardAddMinutes": 15,
 *   "mobileChangeMinutes": 10
 * }
 * }</pre>
 *
 * <p>Missing key or invalid fields use {@value #DEFAULT_TTL_MINUTES} minute default; minimum 1 minute.
 */
public class FaceVerifyTicketTtlConfigLoader {
    public static final String CONFIG_KEY = "face_verify_ticket_ttl";
    private static final int DEFAULT_TTL_MINUTES = 5;
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(DEFAULT_TTL_MINUTES);

    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public FaceVerifyTicketTtlConfigLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public Duration deviceSwitchLoginTtl() {
        return loadSettings().deviceSwitchLoginTtl();
    }

    public Duration bankCardAddTtl() {
        return loadSettings().bankCardAddTtl();
    }

    public Duration mobileChangeTtl() {
        return loadSettings().mobileChangeTtl();
    }

    private Settings loadSettings() {
        return appConfigRepository.findByKey(CONFIG_KEY)
                .map(this::parseSettings)
                .orElse(Settings.defaults());
    }

    private Settings parseSettings(AppConfigRepository.AppConfigRecord record) {
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            if (root == null || root.isNull()) {
                return Settings.defaults();
            }
            if (root.isNumber()) {
                Duration ttl = minutesToDuration(root.asInt());
                return Settings.uniform(ttl);
            }
            if (root.isTextual()) {
                Duration ttl = minutesToDuration(Integer.parseInt(root.asText().trim()));
                return Settings.uniform(ttl);
            }
            Duration defaultTtl = readMinutes(root, "ttlMinutes", DEFAULT_TTL);
            return new Settings(
                    defaultTtl,
                    readOptionalMinutes(root, "deviceSwitchLoginMinutes", defaultTtl),
                    readOptionalMinutes(root, "bankCardAddMinutes", defaultTtl),
                    readOptionalMinutes(root, "mobileChangeMinutes", defaultTtl)
            );
        } catch (Exception exception) {
            return Settings.defaults();
        }
    }

    private static Duration readMinutes(JsonNode root, String field, Duration fallback) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull() || !node.isNumber()) {
            return fallback;
        }
        return minutesToDuration(node.asInt());
    }

    private static Duration readOptionalMinutes(JsonNode root, String field, Duration fallback) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return fallback;
        }
        if (!node.isNumber()) {
            return fallback;
        }
        return minutesToDuration(node.asInt());
    }

    private static Duration minutesToDuration(int minutes) {
        if (minutes <= 0) {
            return DEFAULT_TTL;
        }
        Duration value = Duration.ofMinutes(minutes);
        Duration minimum = Duration.ofMinutes(1);
        return value.compareTo(minimum) < 0 ? minimum : value;
    }

    private record Settings(
            Duration defaultTtl,
            Duration deviceSwitchLoginTtl,
            Duration bankCardAddTtl,
            Duration mobileChangeTtl
    ) {
        static Settings defaults() {
            return uniform(DEFAULT_TTL);
        }

        static Settings uniform(Duration ttl) {
            return new Settings(ttl, ttl, ttl, ttl);
        }
    }
}
