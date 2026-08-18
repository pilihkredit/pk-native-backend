package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.loan.port.LenderLoanHistoryPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ApiPartnerLoanHistoryAdapter implements LenderLoanHistoryPort {
    static final String HISTORY_LIST_PATH = ApiPartnerOpenApiPaths.LOAN_HISTORY_LIST;
    static final String BUSINESS_TYPE = "LOAN_HISTORY_LIST";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerLoanHistoryAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderLoanHistoryResult queryHistory(String partnerUserId) {
        String requestBody = ApiPartnerLoanRequestMapper.buildHistoryListBody(partnerUserId);
        ApiPartnerHttpClient.ExchangeResult exchange =
                httpClient.postWithInteraction(HISTORY_LIST_PATH, requestBody, BUSINESS_TYPE, partnerUserId);
        JsonNode data = exchange.data();
        List<LenderLoanHistoryOrder> orders = new ArrayList<>();
        if (data != null && data.isArray()) {
            for (JsonNode item : data) {
                orders.add(toOrder(item));
            }
        }
        return new LenderLoanHistoryResult(exchange.interactionId(), orders);
    }

    private LenderLoanHistoryOrder toOrder(JsonNode item) {
        return new LenderLoanHistoryOrder(
                textOrNull(item.get("loanApplyId")),
                textOrNull(item.get("loanApplyNo")),
                textOrNull(item.get("userId")),
                textOrNull(item.get("applyStatus")),
                textOrNull(item.get("billNo")),
                decimalOrNull(item.get("applyAmt")),
                decimalOrNull(item.get("payAmount")),
                longOrNull(item.get("payTime")),
                longOrNull(item.get("freezeEndTime")),
                longOrNull(item.get("createTime"))
        );
    }

    private String serialize(JsonNode item) {
        if (item == null || item.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception exception) {
            return null;
        }
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
