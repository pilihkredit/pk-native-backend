package com.pk.core.loan;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class LoanAmountValidator {
    private LoanAmountValidator() {
    }

    public static void validateAgainstCreditLimits(
            BigDecimal applyAmt,
            BigDecimal riskMinLimit,
            BigDecimal fakeCreditLimit,
            BigDecimal borrowAmtStepSize
    ) {
        if (applyAmt == null || applyAmt.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ApiCode.INVALID_LOAN_AMOUNT);
        }
        if (applyAmt.scale() > 2) {
            throw new ApiException(ApiCode.INVALID_LOAN_AMOUNT);
        }
        if (riskMinLimit == null || fakeCreditLimit == null || borrowAmtStepSize == null) {
            throw new ApiException(ApiCode.CREDIT_LIMIT_NOT_AVAILABLE);
        }
        if (applyAmt.compareTo(riskMinLimit) < 0 || applyAmt.compareTo(fakeCreditLimit) > 0) {
            throw new ApiException(ApiCode.INVALID_LOAN_AMOUNT);
        }
        if (borrowAmtStepSize.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ApiCode.CREDIT_LIMIT_NOT_AVAILABLE);
        }
        BigDecimal remainder = applyAmt.subtract(riskMinLimit).remainder(borrowAmtStepSize);
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            throw new ApiException(ApiCode.INVALID_LOAN_AMOUNT);
        }
    }

    public static BigDecimal normalize(BigDecimal applyAmt) {
        return applyAmt.setScale(2, RoundingMode.HALF_UP);
    }
}
