package com.pk.core.loan;

import java.math.BigDecimal;
import java.util.List;

public record LenderLoanProduct(
        String productCode,
        String productName,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String comprehensiveRateUnit,
        BigDecimal comprehensiveRate,
        List<LenderRepayMethod> repayMethods
) {
}
