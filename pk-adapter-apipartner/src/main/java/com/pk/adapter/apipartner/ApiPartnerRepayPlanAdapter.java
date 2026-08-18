package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.LenderRepayPlanTerm;
import com.pk.core.repay.port.LenderRepayPlanPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ApiPartnerRepayPlanAdapter implements LenderRepayPlanPort {
    static final String REPAY_PLAN_PATH = ApiPartnerOpenApiPaths.REPAY_PLAN;
    static final String BUSINESS_TYPE = "REPAY_PLAN";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerRepayPlanAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderRepayPlanResult fetchPlan(String loanApplyId) {
        String requestBody = "{\"loanApplyId\":\"" + loanApplyId + "\"}";
        ApiPartnerHttpClient.ExchangeResult exchange =
                httpClient.postWithInteraction(REPAY_PLAN_PATH, requestBody, BUSINESS_TYPE, loanApplyId);
        JsonNode data = exchange.data();
        return new LenderRepayPlanResult(
                ApiPartnerJsonSupport.requireText(data.get("loanApplyId"), "loanApplyId"),
                ApiPartnerJsonSupport.requireText(data.get("loanApplyNo"), "loanApplyNo"),
                ApiPartnerJsonSupport.requireText(data.get("billNo"), "billNo"),
                mapTerms(data.get("terms")),
                exchange.interactionId()
        );
    }

    private List<LenderRepayPlanTerm> mapTerms(JsonNode termsNode) {
        if (termsNode == null || !termsNode.isArray() || termsNode.isEmpty()) {
            throw new com.pk.core.api.ApiException(com.pk.core.api.ApiCode.SERVICE_UNAVAILABLE);
        }
        List<LenderRepayPlanTerm> terms = new ArrayList<>();
        for (JsonNode termNode : termsNode) {
            int termNo = ApiPartnerJsonSupport.requireInt(termNode.get("termNo"), "termNo");
            terms.add(new LenderRepayPlanTerm(
                    termNo,
                    millisToInstant(ApiPartnerJsonSupport.requireLong(termNode.get("dueDate"), "dueDate")),
                    millisToInstant(ApiPartnerJsonSupport.longOrNull(termNode.get("valueDate"))),
                    millisToInstant(ApiPartnerJsonSupport.longOrNull(termNode.get("graceDate"))),
                    ApiPartnerJsonSupport.requireText(termNode.get("subBillNo"), "subBillNo"),
                    ApiPartnerJsonSupport.requireText(termNode.get("termStatus"), "termStatus"),
                    ApiPartnerJsonSupport.textOrNull(termNode.get("partRepayFlag")),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("schdAmount"), "schdAmount"),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("shouldAmount"), "shouldAmount"),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("shouldPrincipal"), "shouldPrincipal"),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("shouldInterest"), "shouldInterest"),
                    ApiPartnerJsonSupport.requireDecimal(termNode.get("paidAmount"), "paidAmount"),
                    ApiPartnerJsonSupport.requireInt(termNode.get("overdueDays"), "overdueDays"),
                    millisToInstant(ApiPartnerJsonSupport.longOrNull(termNode.get("lastRepayTime"))),
                    ApiPartnerJsonSupport.textOrNull(termNode.get("advSetteFlag")),
                    termNode.toString()
            ));
        }
        return List.copyOf(terms);
    }

    private static Instant millisToInstant(Long epochMillis) {
        return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis);
    }
}
