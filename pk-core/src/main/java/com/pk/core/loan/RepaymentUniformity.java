package com.pk.core.loan;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RepaymentUniformity {
    private RepaymentUniformity() {
    }

    /**
     * Whether each installment has the same repayment days and principal ratio.
     * Aligns with KTA product flow: even products use fake-credit default slider;
     * uneven products use max visible limit.
     */
    public static boolean isUniform(LenderRepayMethod repayMethod) {
        if (repayMethod == null) {
            return true;
        }
        if (repayMethod.repayMethodType() != null && repayMethod.repayMethodType() == 1) {
            return false;
        }
        List<LenderRepayMethod.UnevenBillRate> rates = repayMethod.unevenBillsRepaymentRates();
        if (rates == null || rates.isEmpty()) {
            return true;
        }
        Set<BigDecimal> distinctPositiveRates = new HashSet<>();
        for (LenderRepayMethod.UnevenBillRate rate : rates) {
            if (rate.repaymentRate() == null || rate.repaymentRate().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            distinctPositiveRates.add(rate.repaymentRate().stripTrailingZeros());
        }
        return distinctPositiveRates.size() <= 1;
    }
}
