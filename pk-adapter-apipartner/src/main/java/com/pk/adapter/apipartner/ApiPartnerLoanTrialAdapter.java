package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.loan.port.LenderLoanTrialPort;

public class ApiPartnerLoanTrialAdapter implements LenderLoanTrialPort {
    static final String LOAN_TRIAL_PATH = ApiPartnerOpenApiPaths.LOAN_TRIAL;
    static final String BUSINESS_TYPE = "LOAN_TRIAL";

    private final ApiPartnerHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiPartnerLoanTrialAdapter(ApiPartnerHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LenderLoanTrialResult trial(LenderLoanTrialCommand command) {
        String requestBody = buildRequestBody(command);
        ApiPartnerHttpClient.ExchangeResult exchange = httpClient.postWithInteraction(
                LOAN_TRIAL_PATH,
                requestBody,
                BUSINESS_TYPE,
                command.applyId()
        );
        JsonNode data = exchange.data();
        return new LenderLoanTrialResult(
                ApiPartnerLoanTrialParser.mapQuote(data),
                ApiPartnerLoanTrialParser.mapTerms(data == null ? null : data.get("termInfo")),
                exchange.interactionId()
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
}
