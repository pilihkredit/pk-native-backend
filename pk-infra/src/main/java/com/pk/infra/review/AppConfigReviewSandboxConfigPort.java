package com.pk.infra.review;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.review.ReviewSandboxConfigPort;
import com.pk.core.review.ReviewSandboxConfigPort.ReviewSandboxVa;
import com.pk.core.review.ReviewSandboxConfigPort.ReviewSandboxVaChannel;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class AppConfigReviewSandboxConfigPort implements ReviewSandboxConfigPort {
    static final String CONFIG_KEY = "reviewSandboxConf";
    private final AppConfigRepository repository;
    private final ObjectMapper objectMapper;

    public AppConfigReviewSandboxConfigPort(AppConfigRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ReviewSandboxScenario> findEnabledScenario(String mobileNo) {
        if (mobileNo == null || mobileNo.isBlank()) {
            return Optional.empty();
        }
        Map<String, ReviewSandboxScenario> scenariosByMobile = repository.findByKey(CONFIG_KEY)
                .map(AppConfigRepository.AppConfigRecord::valueJson)
                .map(this::parse)
                .orElseGet(Map::of);
        return Optional.ofNullable(scenariosByMobile.get(mobileNo.trim()));
    }

    private Map<String, ReviewSandboxScenario> parse(String valueJson) {
        try {
            JsonNode root = objectMapper.readTree(valueJson);
            if (!root.path("enabled").asBoolean(false)) {
                return Map.of();
            }
            JsonNode scenariosNode = root.path("scenarios");
            Map<String, ReviewSandboxScenario> result = new HashMap<>();
            for (JsonNode user : root.path("users")) {
                String mobileNo = requiredText(user, "mobileNo");
                String scenarioCode = requiredText(user, "scenarioCode");
                JsonNode scenario = scenariosNode.path(scenarioCode);
                if (scenario.isMissingNode() || scenario.isNull()) {
                    throw new IllegalStateException("Review sandbox scenario not found: " + scenarioCode);
                }
                result.put(mobileNo, toScenario(scenarioCode, scenario));
            }
            return Map.copyOf(result);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid review sandbox configuration", exception);
        }
    }

    private static ReviewSandboxScenario toScenario(String code, JsonNode node) {
        return new ReviewSandboxScenario(
                code,
                requiredDecimal(node, "minAmount"),
                requiredDecimal(node, "maxAmount"),
                requiredDecimal(node, "amountStep"),
                requiredDecimal(node, "comprehensiveRate"),
                requiredDecimal(node, "disbursementRate"),
                requiredPositiveInt(node, "termCount"),
                requiredPositiveInt(node, "termDays"),
                requiredText(node, "vaBankCode"),
                requiredText(node, "vaBankName"),
                requiredText(node, "vaNo"),
                parseVas(node.path("vas")),
                parseResponses(node.path("responses"))
        );
    }

    private static Map<String, String> parseResponses(JsonNode responsesNode) {
        if (!responsesNode.isObject()) {
            return Map.of();
        }
        Map<String, String> responses = new HashMap<>();
        responsesNode.fields().forEachRemaining(entry -> responses.put(entry.getKey(), entry.getValue().toString()));
        return Map.copyOf(responses);
    }

    private static List<ReviewSandboxVa> parseVas(JsonNode nodes) {
        if (!nodes.isArray()) {
            return List.of();
        }
        List<ReviewSandboxVa> vas = new ArrayList<>();
        for (JsonNode node : nodes) {
            List<ReviewSandboxVaChannel> channels = new ArrayList<>();
            for (JsonNode channel : node.path("bankChannels")) {
                channels.add(new ReviewSandboxVaChannel(
                        channel.path("bankChannel").asText(""),
                        channel.path("instruction").asText(""),
                        channel.path("defaultChannel").asBoolean(false)
                ));
            }
            vas.add(new ReviewSandboxVa(
                    requiredText(node, "vaNo"),
                    requiredText(node, "bankCode"),
                    requiredText(node, "bankName"),
                    node.path("bankType").asInt(1),
                    node.path("icon").asText(null),
                    List.copyOf(channels),
                    node.path("defaultFlag").asBoolean(false),
                    node.path("disabled").asBoolean(false),
                    node.path("show").asBoolean(true)
            ));
        }
        long defaults = vas.stream().filter(ReviewSandboxVa::defaultFlag).count();
        if (!vas.isEmpty() && defaults != 1) {
            throw new IllegalStateException("Review sandbox VA list must contain one default VA");
        }
        return List.copyOf(vas);
    }

    private static String requiredText(JsonNode node, String field) {
        String value = node.path(field).asText("").trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("Missing review sandbox field: " + field);
        }
        return value;
    }

    private static BigDecimal requiredDecimal(JsonNode node, String field) {
        if (!node.hasNonNull(field) || !node.get(field).isNumber()) {
            throw new IllegalStateException("Missing review sandbox field: " + field);
        }
        return node.get(field).decimalValue();
    }

    private static int requiredPositiveInt(JsonNode node, String field) {
        int value = node.path(field).asInt(0);
        if (value <= 0) {
            throw new IllegalStateException("Invalid review sandbox field: " + field);
        }
        return value;
    }
}
