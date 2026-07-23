package com.pk.infra.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;

/**
 * Loads max bindable bank cards per user from {@code app_config}.
 *
 * <p>Key {@value #CONFIG_KEY}: JSON number, e.g. {@code 5}.
 */
public class BankCardMaxConfigLoader {
    public static final String CONFIG_KEY = "bank_card_max_count";
    private static final int DEFAULT_MAX_COUNT = 5;

    private final AppConfigRepository appConfigRepository;
    private final ObjectMapper objectMapper;

    public BankCardMaxConfigLoader(AppConfigRepository appConfigRepository, ObjectMapper objectMapper) {
        this.appConfigRepository = appConfigRepository;
        this.objectMapper = objectMapper;
    }

    public int loadMaxCount() {
        return appConfigRepository.findByKey(CONFIG_KEY)
                .map(this::parseMaxCount)
                .orElse(DEFAULT_MAX_COUNT);
    }

    private int parseMaxCount(AppConfigRepository.AppConfigRecord record) {
        try {
            JsonNode root = objectMapper.readTree(record.valueJson());
            int limit;
            if (root != null && root.isNumber()) {
                limit = root.asInt();
            } else if (root != null && root.isTextual()) {
                limit = Integer.parseInt(root.asText().trim());
            } else if (root != null && root.isObject() && root.has("maxCount") && root.get("maxCount").isNumber()) {
                limit = root.get("maxCount").asInt();
            } else {
                return DEFAULT_MAX_COUNT;
            }
            return limit > 0 ? limit : DEFAULT_MAX_COUNT;
        } catch (Exception exception) {
            return DEFAULT_MAX_COUNT;
        }
    }
}
