package com.pk.core.repay;

import java.math.BigDecimal;
import java.time.Instant;

public record LenderRepayPlanTerm(
        int termNo,
        Instant dueDate,
        Instant valueDate,
        Instant graceDate,
        String subBillNo,
        String termStatus,
        String partRepayFlag,
        BigDecimal schdAmount,
        BigDecimal shouldAmount,
        BigDecimal shouldPrincipal,
        BigDecimal shouldInterest,
        BigDecimal paidAmount,
        Integer overdueDays,
        Instant lastRepayTime,
        String advSetteFlag,
        String amountDetailJson
) {
}
