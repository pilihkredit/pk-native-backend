package com.pk.core.repay;

import java.math.BigDecimal;
import java.time.Instant;

public record LenderRepayTrialTerm(
        String billNo,
        String loanApplyNo,
        int termNo,
        String advSetteFlag,
        BigDecimal schdAmount,
        Instant dueDate,
        Integer overdueDays,
        Instant graceDate,
        String termStatus,
        Long daysOfDueDate,
        BigDecimal shouldAmount,
        BigDecimal shouldPrincipal,
        BigDecimal shouldInterest,
        BigDecimal shouldPenInterest,
        BigDecimal shouldInitLateFee,
        String partRepayFlag,
        BigDecimal paidAmount,
        BigDecimal couponDiscount,
        BigDecimal reductionAmount,
        String termDetailJson
) {
}
