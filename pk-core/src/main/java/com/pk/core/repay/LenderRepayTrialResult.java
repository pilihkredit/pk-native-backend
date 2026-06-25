package com.pk.core.repay;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record LenderRepayTrialResult(
        String loanApplyId,
        String loanApplyNo,
        String billNo,
        String billStatus,
        String name,
        String userId,
        String advSetteFlag,
        String currency,
        BigDecimal applyAmt,
        Instant applyTime,
        Instant auditTime,
        Instant loanTime,
        Integer loanDays,
        Integer repayMethodType,
        BigDecimal schdAmount,
        BigDecimal shouldAmount,
        BigDecimal shouldPrincipal,
        BigDecimal shouldInterest,
        BigDecimal shouldFee,
        BigDecimal shouldPenInterest,
        BigDecimal shouldInitLateFee,
        BigDecimal shouldPenalty,
        BigDecimal shouldAdvSettleFee,
        BigDecimal reductionAmount,
        BigDecimal paidAmount,
        LenderRepayVa defaultVa,
        LenderRepayVa spareVa,
        LenderRepayVa disabledDefaultVa,
        Long couponId,
        String couponType,
        String couponName,
        Long userValideDisTime,
        Integer termNo,
        Instant termDueDate,
        List<LenderRepayTrialTerm> termInfo,
        String rawResponseJson
) {
}
