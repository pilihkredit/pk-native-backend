package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.callback.port.CreditCallbackParser;
import java.math.BigDecimal;

public class PendanaanCreditCallbackParser implements CreditCallbackParser {
    private final ObjectMapper objectMapper;

    public PendanaanCreditCallbackParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ParsedCreditCallback parse(String rawPayloadJson) {
        try {
            JsonNode root = objectMapper.readTree(rawPayloadJson);
            String applyId = requiredText(root, "applyId");
            return new ParsedCreditCallback(
                    applyId,
                    textOrNull(root.get("creditApplyNo")),
                    requiredText(root, "status"),
                    longOrNull(root.get("creditContractExpireTime")),
                    decimalOrNull(root.get("riskMinLimit")),
                    decimalOrNull(root.get("riskMaxLimit")),
                    decimalOrNull(root.get("psychologicalCreditLimit")),
                    decimalOrNull(root.get("fakeCreditLimit")),
                    decimalOrNull(root.get("borrowAmtStepSize"))
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

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }

    private static Long longOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asLong();
    }

    private static BigDecimal decimalOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }
}
