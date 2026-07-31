package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.callback.port.ServerEventCallbackParser;
import java.math.BigDecimal;

public class PendanaanServerEventCallbackParser implements ServerEventCallbackParser {
    private final ObjectMapper objectMapper;

    public PendanaanServerEventCallbackParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ParsedServerEventCallback parse(String rawPayloadJson) {
        try {
            JsonNode root = objectMapper.readTree(rawPayloadJson);
            return new ParsedServerEventCallback(
                    requiredText(root, "eventId"),
                    requiredText(root, "eventType"),
                    requiredLong(root, "eventTime"),
                    textOrNull(root.get("eventValue")),
                    decimalOrNull(root.get("value")),
                    textOrNull(root.get("clientId")),
                    textOrNull(root.get("userId")),
                    textOrNull(root.get("partnerUserId")),
                    textOrNull(root.get("appName")),
                    textOrNull(root.get("countryCode")),
                    textOrNull(root.get("appVersion")),
                    textOrNull(root.get("countryName")),
                    textOrNull(root.get("deviceNo")),
                    textOrNull(root.get("systemPlatform")),
                    textOrNull(root.get("adId"))
            );
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, exception);
        }
    }

    private static String requiredText(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        String value = node.asText();
        if (value.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return value;
    }

    private static Long requiredLong(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull() || !node.canConvertToLong()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return node.asLong();
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }

    private static BigDecimal decimalOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }
}
