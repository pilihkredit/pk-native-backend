package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.loan.port.LenderLoanApplyPort;

public class PendanaanLoanApplyAdapter implements LenderLoanApplyPort {
    static final String APPLY_PATH = "/api/open/v1/loan/apply";
    static final String BUSINESS_TYPE = "LOAN_APPLY";

    private final PendanaanHttpClient httpClient;

    public PendanaanLoanApplyAdapter(PendanaanHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public LenderLoanApplyResult apply(LenderLoanApplyCommand command) {
        String requestBody = PendanaanLoanRequestMapper.buildApplyBody(command);
        JsonNode envelope = httpClient.postEnvelope(
                APPLY_PATH,
                requestBody,
                BUSINESS_TYPE,
                command.loanApplyId()
        );
        String responseCode = PendanaanHttpSupport.textOrEmpty(envelope.get("code"));
        if (!ApiCode.SUCCESS.code().equals(responseCode)) {
            throw PendanaanHttpSupport.mapFailureCode(responseCode);
        }
        JsonNode data = envelope.get("data");
        if (data == null || data.isNull()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return new LenderLoanApplyResult(
                requiredText(data, "loanApplyNo"),
                requiredText(data, "applyStatus")
        );
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
}
