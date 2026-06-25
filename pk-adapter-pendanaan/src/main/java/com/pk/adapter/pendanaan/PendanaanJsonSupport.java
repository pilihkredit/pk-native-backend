package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.math.BigDecimal;

final class PendanaanJsonSupport {
    private PendanaanJsonSupport() {
    }

    static String requireText(JsonNode node, String fieldName) {
        String value = textOrNull(node);
        if (value == null) {
            throw missingField(fieldName);
        }
        return value;
    }

    static int requireInt(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            throw missingField(fieldName);
        }
        return node.asInt();
    }

    static long requireLong(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            throw missingField(fieldName);
        }
        return node.asLong();
    }

    static BigDecimal requireDecimal(JsonNode node, String fieldName) {
        BigDecimal value = decimalOrNull(node);
        if (value == null) {
            throw missingField(fieldName);
        }
        return value;
    }

    static boolean requireBoolean(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            throw missingField(fieldName);
        }
        return node.asBoolean();
    }

    static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }

    static Integer intOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asInt();
    }

    static Long longOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asLong();
    }

    static BigDecimal decimalOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }

    static Boolean booleanOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asBoolean();
    }

    private static ApiException missingField(String fieldName) {
        return new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
