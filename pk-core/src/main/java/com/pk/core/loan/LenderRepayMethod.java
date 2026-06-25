package com.pk.core.loan;

import java.math.BigDecimal;
import java.util.List;

public record LenderRepayMethod(
        String repayMethod,
        String cycleType,
        Integer cycleInterval,
        Integer cycleCount,
        Integer totalCycleInterval,
        Integer repayMethodType,
        List<UnevenBillRate> unevenBillsRepaymentRates
) {
    public record UnevenBillRate(int termNum, BigDecimal repaymentRate) {
    }
}
