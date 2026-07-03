package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.LenderRepayPlanTerm;
import com.pk.core.repay.port.LenderRepayPlanPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PendanaanRepayPlanAdapter implements LenderRepayPlanPort {
    static final String REPAY_PLAN_PATH = PendanaanOpenApiPaths.REPAY_PLAN;
    static final String BUSINESS_TYPE = "REPAY_PLAN";

    private final PendanaanHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PendanaanRepayPlanAdapter(PendanaanHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderRepayPlanResult fetchPlan(String loanApplyId) {
        String requestBody = "{\"loanApplyId\":\"" + loanApplyId + "\"}";
        JsonNode data = httpClient.post(REPAY_PLAN_PATH, requestBody, BUSINESS_TYPE, loanApplyId);
        String rawResponseJson = data == null ? "{}" : data.toString();
        return new LenderRepayPlanResult(
                PendanaanJsonSupport.requireText(data.get("loanApplyId"), "loanApplyId"),
                PendanaanJsonSupport.requireText(data.get("loanApplyNo"), "loanApplyNo"),
                PendanaanJsonSupport.requireText(data.get("billNo"), "billNo"),
                mapTerms(data.get("terms")),
                requestBody,
                rawResponseJson
        );
    }

    private List<LenderRepayPlanTerm> mapTerms(JsonNode termsNode) {
        if (termsNode == null || !termsNode.isArray() || termsNode.isEmpty()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderRepayPlanTerm> terms = new ArrayList<>();
        for (JsonNode termNode : termsNode) {
            int termNo = PendanaanJsonSupport.requireInt(termNode.get("termNo"), "termNo");
            terms.add(new LenderRepayPlanTerm(
                    termNo,
                    millisToInstant(PendanaanJsonSupport.requireLong(termNode.get("dueDate"), "dueDate")),
                    millisToInstant(PendanaanJsonSupport.longOrNull(termNode.get("valueDate"))),
                    millisToInstant(PendanaanJsonSupport.longOrNull(termNode.get("graceDate"))),
                    PendanaanJsonSupport.requireText(termNode.get("subBillNo"), "subBillNo"),
                    PendanaanJsonSupport.requireText(termNode.get("termStatus"), "termStatus"),
                    PendanaanJsonSupport.textOrNull(termNode.get("partRepayFlag")),
                    PendanaanJsonSupport.requireDecimal(termNode.get("schdAmount"), "schdAmount"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("shouldAmount"), "shouldAmount"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("shouldPrincipal"), "shouldPrincipal"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("shouldInterest"), "shouldInterest"),
                    PendanaanJsonSupport.requireDecimal(termNode.get("paidAmount"), "paidAmount"),
                    PendanaanJsonSupport.requireInt(termNode.get("overdueDays"), "overdueDays"),
                    millisToInstant(PendanaanJsonSupport.longOrNull(termNode.get("lastRepayTime"))),
                    PendanaanJsonSupport.textOrNull(termNode.get("advSetteFlag")),
                    termNode.toString()
            ));
        }
        return List.copyOf(terms);
    }

    private static Instant millisToInstant(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis);
    }
}
