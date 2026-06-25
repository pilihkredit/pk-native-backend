package com.pk.core.callback.port;

import java.math.BigDecimal;

public interface CreditCallbackParser {
    ParsedCreditCallback parse(String rawPayloadJson);

    record ParsedCreditCallback(
            String applyId,
            String creditApplyNo,
            String externalStatus,
            Long creditContractExpireTime,
            BigDecimal riskMinLimit,
            BigDecimal riskMaxLimit,
            BigDecimal psychologicalCreditLimit,
            BigDecimal fakeCreditLimit,
            BigDecimal borrowAmtStepSize
    ) {
    }
}
