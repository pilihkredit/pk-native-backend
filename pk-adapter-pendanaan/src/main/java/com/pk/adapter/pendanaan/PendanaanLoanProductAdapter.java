package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.loan.LenderLoanProduct;
import com.pk.core.loan.LenderRepayMethod;
import com.pk.core.loan.port.LenderLoanProductPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PendanaanLoanProductAdapter implements LenderLoanProductPort {
    static final String PRODUCT_LIST_PATH = PendanaanOpenApiPaths.PRODUCT_LIST;
    static final String BUSINESS_TYPE = "LOAN_PRODUCT_LIST";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanLoanProductAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderLoanProductListResult listProducts(String applyId) {
        String requestBody = "{\"applyId\":\"" + applyId + "\"}";
        JsonNode data = httpClient.post(PRODUCT_LIST_PATH, requestBody, BUSINESS_TYPE, applyId);
        return new LenderLoanProductListResult(
                textOrNull(data.get("applyId")),
                textOrNull(data.get("creditApplyNo")),
                textOrNull(data.get("userId")),
                textOrNull(data.get("creditStatus")),
                textOrNull(data.get("productStatus")),
                mapProducts(data.get("products")),
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
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }

    private List<LenderLoanProduct> mapProducts(JsonNode productsNode) {
        if (productsNode == null || !productsNode.isArray()) {
            return List.of();
        }
        List<LenderLoanProduct> products = new ArrayList<>();
        for (JsonNode productNode : productsNode) {
            products.add(new LenderLoanProduct(
                    textOrNull(productNode.get("productCode")),
                    textOrNull(productNode.get("productName")),
                    decimalOrNull(productNode.get("minAmount")),
                    decimalOrNull(productNode.get("maxAmount")),
                    textOrNull(productNode.get("comprehensiveRateUnit")),
                    decimalOrNull(productNode.get("comprehensiveRate")),
                    mapRepayMethods(productNode.get("repayMethods"))
            ));
        }
        return List.copyOf(products);
    }

    private List<LenderRepayMethod> mapRepayMethods(JsonNode repayMethodsNode) {
        if (repayMethodsNode == null || !repayMethodsNode.isArray()) {
            return List.of();
        }
        List<LenderRepayMethod> repayMethods = new ArrayList<>();
        for (JsonNode methodNode : repayMethodsNode) {
            JsonNode unevenNode = methodNode.get("unevenBillsRepaymentRate");
            repayMethods.add(new LenderRepayMethod(
                    textOrNull(methodNode.get("repayMethod")),
                    textOrNull(methodNode.get("cycleType")),
                    intOrNull(methodNode.get("cycleInterval")),
                    intOrNull(methodNode.get("cycleCount")),
                    intOrNull(methodNode.get("totalCycleInterval")),
                    intOrNull(methodNode.get("repayMethodType")),
                    rawUnevenJson(unevenNode),
                    parseUnevenRates(unevenNode)
            ));
        }
        return List.copyOf(repayMethods);
    }

    private static String rawUnevenJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            String value = node.asText();
            return value.isBlank() ? null : value;
        }
        return node.toString();
    }

    private List<LenderRepayMethod.UnevenBillRate> parseUnevenRates(JsonNode node) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        try {
            String raw = node.isTextual() ? node.asText() : node.toString();
            if (raw == null || raw.isBlank()) {
                return List.of();
            }
            List<RateItem> items = objectMapper.readValue(raw, new TypeReference<>() {
            });
            return items.stream()
                    .map(item -> new LenderRepayMethod.UnevenBillRate(item.termNum, item.repaymentRate))
                    .toList();
        } catch (Exception exception) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE, exception);
        }
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }

    private static Integer intOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asInt();
    }

    private static BigDecimal decimalOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }

    private record RateItem(int termNum, BigDecimal repaymentRate) {
    }
}
