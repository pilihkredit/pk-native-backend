package com.pk.app.credit.dto.response;

import java.math.BigDecimal;

public record CreditStatusResponse(
        String applyId,
        String status,
        String creditApplyNo,
        Long creditContractExpireTime,
        Long freezeEndTime,
        BigDecimal riskMinLimit,
        BigDecimal riskMaxLimit,
        BigDecimal psychologicalCreditLimit,
        BigDecimal fakeCreditLimit,
        BigDecimal borrowAmtStepSize
) {
}
