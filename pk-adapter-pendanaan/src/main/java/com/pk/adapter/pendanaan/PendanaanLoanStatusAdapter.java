package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.loan.port.LenderLoanStatusPort;
import java.math.BigDecimal;

public class PendanaanLoanStatusAdapter implements LenderLoanStatusPort {
    static final String APPLY_STATUS_PATH = PendanaanOpenApiPaths.LOAN_APPLY_STATUS;
    static final String BUSINESS_TYPE = "LOAN_APPLY_STATUS";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanLoanStatusAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderLoanStatusResult queryStatus(String loanApplyId) {
        String requestBody = PendanaanLoanRequestMapper.buildStatusBody(loanApplyId);
        JsonNode data = httpClient.post(APPLY_STATUS_PATH, requestBody, BUSINESS_TYPE, loanApplyId);
        if (data == null || data.isNull()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return new LenderLoanStatusResult(
                requiredText(data, "applyStatus"),
                textOrNull(data.get("loanApplyNo")),
                textOrNull(data.get("billNo")),
                decimalOrNull(data.get("applyAmt")),
                decimalOrNull(data.get("payAmount")),
                longOrNull(data.get("payTime")),
                requestBody,
                serializeResponseData(data)
        );
    }

    private String serializeResponseData(JsonNode data) {
        if (data == null || data.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception exception) {
            return null;
        }
    }

    private static String requiredText(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        String value = valueNode.asText();
        if (value.isBlank()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
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
