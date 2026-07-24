package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.port.LenderLoanBillListPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PendanaanLoanBillListAdapter implements LenderLoanBillListPort {
    static final String BILL_LIST_PATH = PendanaanOpenApiPaths.LOAN_BILL_LIST;
    static final String BUSINESS_TYPE = "LOAN_BILL_LIST";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanLoanBillListAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderLoanBillListResult listBills(String partnerUserId, List<String> billStatuses) {
        String requestBody = PendanaanLoanRequestMapper.buildBillListBody(partnerUserId, billStatuses);
        PendanaanHttpClient.ExchangeResult exchange =
                httpClient.postWithInteraction(BILL_LIST_PATH, requestBody, BUSINESS_TYPE, partnerUserId);
        JsonNode data = exchange.data();
        List<LenderLoanBill> bills = new ArrayList<>();
        if (data != null && data.isArray()) {
            for (JsonNode item : data) {
                bills.add(toBill(item));
            }
        }
        return new LenderLoanBillListResult(exchange.interactionId(), bills);
    }

    private LenderLoanBill toBill(JsonNode item) {
        return new LenderLoanBill(
                textOrNull(item.get("loanApplyId")),
                textOrNull(item.get("loanApplyNo")),
                textOrNull(item.get("userId")),
                textOrNull(item.get("billNo")),
                decimalOrNull(item.get("applyAmt")),
                textOrNull(item.get("billStatus")),
                longOrNull(item.get("termDueDate")),
                sumShouldAmounts(item)
        );
    }

    private BigDecimal sumShouldAmounts(JsonNode item) {
        BigDecimal total = BigDecimal.ZERO;
        String[] fields = {
                "shouldStampDuty",
                "shouldPrincipal",
                "shouldInterest",
                "shouldFee1",
                "shouldFee2",
                "shouldFee3",
                "shouldFee1Tax",
                "shouldFee2Tax",
                "shouldFee3Tax",
                "shouldPenInterest",
                "shouldInitLateFee"
        };
        for (String field : fields) {
            BigDecimal value = decimalOrNull(item.get(field));
            if (value != null) {
                total = total.add(value);
            }
        }
        return total;
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
