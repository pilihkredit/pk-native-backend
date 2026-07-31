package com.pk.infra.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;

final class LenderProviderConfigJson {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LenderProviderConfigJson() {
    }

    static Optional<String> readText(String configJson, String... keys) {
        if (configJson == null || configJson.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(configJson);
            for (String key : keys) {
                JsonNode node = root.get(key);
                if (node != null && !node.isNull()) {
                    String value = node.asText("").trim();
                    if (!value.isEmpty()) {
                        return Optional.of(value);
                    }
                }
            }
            return Optional.empty();
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
