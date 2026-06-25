package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.port.LenderCreditPort;
import java.math.BigDecimal;

public class PendanaanCreditAdapter implements LenderCreditPort {
    static final String APPLY_PATH = "/api/open/v1/credit/apply";
    static final String APPLY_STATUS_PATH = "/api/open/v1/credit/applyStatus";
    static final String BUSINESS_TYPE_APPLY = "CREDIT_APPLY";
    static final String BUSINESS_TYPE_STATUS = "CREDIT_APPLY_STATUS";

    private final PendanaanHttpClient httpClient;

    public PendanaanCreditAdapter(PendanaanHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public LenderCreditApplyResult apply(LenderCreditApplyCommand command) {
        String requestBody = PendanaanCreditRequestMapper.buildApplyBody(command);
        JsonNode envelope = httpClient.postEnvelope(
                APPLY_PATH,
                requestBody,
                BUSINESS_TYPE_APPLY,
                command.applyId()
        );
        String responseCode = PendanaanHttpSupport.textOrEmpty(envelope.get("code"));
        if (!ApiCode.SUCCESS.code().equals(responseCode)) {
            throw PendanaanHttpSupport.mapFailureCode(responseCode);
        }
        JsonNode data = envelope.get("data");
        if (data == null || data.isNull()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return new LenderCreditApplyResult(
                PendanaanHttpSupport.textOrEmpty(data.get("creditApplyNo")),
                textOrNull(data.get("userId"))
        );
    }

    @Override
    public LenderCreditStatusResult queryStatus(String applyId) {
        String requestBody = PendanaanCreditRequestMapper.buildStatusBody(applyId);
        JsonNode data = httpClient.post(
                APPLY_STATUS_PATH,
                requestBody,
                BUSINESS_TYPE_STATUS,
                applyId
        );
        return new LenderCreditStatusResult(
                PendanaanHttpSupport.textOrEmpty(data.get("status")),
                textOrNull(data.get("creditApplyNo")),
                longOrNull(data.get("creditContractExpireTime")),
                decimalOrNull(data.get("riskMinLimit")),
                decimalOrNull(data.get("riskMaxLimit")),
                decimalOrNull(data.get("psychologicalCreditLimit")),
                decimalOrNull(data.get("fakeCreditLimit")),
                decimalOrNull(data.get("borrowAmtStepSize"))
        );
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
