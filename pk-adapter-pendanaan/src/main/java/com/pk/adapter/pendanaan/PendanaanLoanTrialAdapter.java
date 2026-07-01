package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.port.LenderLoanTrialPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PendanaanLoanTrialAdapter implements LenderLoanTrialPort {
    static final String LOAN_TRIAL_PATH = PendanaanOpenApiPaths.LOAN_TRIAL;
    static final String BUSINESS_TYPE = "LOAN_TRIAL";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanLoanTrialAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderLoanTrialResult trial(LenderLoanTrialCommand command) {
        String requestBody = buildRequestBody(command);
        JsonNode data = httpClient.post(LOAN_TRIAL_PATH, requestBody, BUSINESS_TYPE, command.applyId());
        String rawResponseJson = data == null ? "{}" : data.toString();
        return new LenderLoanTrialResult(
                decimalOrNull(data == null ? null : data.get("applyAmt")),
                decimalOrNull(data == null ? null : data.get("payAmount")),
                decimalOrNull(data == null ? null : data.get("schdAmount")),
                decimalOrNull(data == null ? null : data.get("interest")),
                intOrNull(data == null ? null : data.get("loanTerm")),
                decimalOrNull(data == null ? null : data.get("loanPrincipal")),
                intOrNull(data == null ? null : data.get("totalDays")),
                mapTerms(data == null ? null : data.get("termInfo")),
                rawResponseJson
        );
    }

    private String buildRequestBody(LenderLoanTrialCommand command) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("applyId", command.applyId());
            root.put("applyAmt", command.applyAmt());
            root.put("productCode", command.productCode());
            root.put("repayMethod", command.repayMethod());
            if (command.couponId() != null) {
                root.put("couponId", command.couponId());
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to build loan trial request", exception);
        }
    }

    private List<LenderTrialTerm> mapTerms(JsonNode termInfoNode) {
        if (termInfoNode == null || !termInfoNode.isArray()) {
            return List.of();
        }
        List<LenderTrialTerm> terms = new ArrayList<>();
        for (JsonNode termNode : termInfoNode) {
            Integer termNo = intOrNull(termNode.get("termNo"));
            if (termNo == null) {
                continue;
            }
            terms.add(new LenderTrialTerm(
                    termNo,
                    millisToInstant(longOrNull(termNode.get("dueDate"))),
                    decimalOrNull(termNode.get("schdAmount")),
                    decimalOrNull(termNode.get("schdPrincipal")),
                    decimalOrNull(termNode.get("schdInterest"))
            ));
        }
        return List.copyOf(terms);
    }

    private static Instant millisToInstant(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis);
    }

    private static Integer intOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asInt();
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
