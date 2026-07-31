package com.pk.core.repay;

import java.math.BigDecimal;

public record LenderRepayTrialDiscountInfo(
        BigDecimal reductionAllAmt,
        String reductionType,
        BigDecimal reductionStampDuty,
        BigDecimal reductionPrincipal,
        BigDecimal reductionInterest,
        BigDecimal reductionFee1,
        BigDecimal reductionFee2,
        BigDecimal reductionFee3,
        BigDecimal reductionFee1Tax,
        BigDecimal reductionFee2Tax,
        BigDecimal reductionFee3Tax,
        BigDecimal reductionPenInterest,
        BigDecimal reductionInitLateFee,
        Long couponId,
        String couponType
) {
}
