package com.pk.core.loan;

import java.math.BigDecimal;

public record LenderTrialTerm(
        int termNo,
        Long valueDate,
        Long dueDate,
        Long graceDate,
        BigDecimal schdAmount,
        BigDecimal schdPrincipal,
        BigDecimal showLoanPrincipal,
        BigDecimal schdInterest,
        BigDecimal showInterest,
        BigDecimal shouldAmount,
        BigDecimal shouldPrincipal,
        BigDecimal shouldInterest,
        BigDecimal fee1,
        BigDecimal fee2,
        BigDecimal fee3,
        BigDecimal fee1Tax,
        BigDecimal fee2Tax,
        BigDecimal fee3Tax,
        BigDecimal stampDuty,
        BigDecimal shouldStampDuty,
        BigDecimal reductionAmount,
        BigDecimal reductionPrincipal,
        BigDecimal reductionInterest,
        BigDecimal reductionFee1,
        BigDecimal reductionFee2,
        BigDecimal reductionFee3,
        BigDecimal reductionFee1Tax,
        BigDecimal reductionFee2Tax,
        BigDecimal reductionFee3Tax,
        BigDecimal reductionStampDuty
) {
}
