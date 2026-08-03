package com.pk.core.loan;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class LoanAmountValidatorTest {
    @Test
    void acceptsAmountWithinLimitsAndStepSize() {
        assertDoesNotThrow(() -> LoanAmountValidator.validateAgainstCreditLimits(
                new BigDecimal("1500000"),
                new BigDecimal("500000"),
                new BigDecimal("2800000"),
                new BigDecimal("100000")
        ));
    }

    @Test
    void rejectsAmountAboveFakeLimit() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> LoanAmountValidator.validateAgainstCreditLimits(
                        new BigDecimal("3000000"),
                        new BigDecimal("500000"),
                        new BigDecimal("2800000"),
                        new BigDecimal("100000")
                )
        );
        assertEquals(ApiCode.INVALID_LOAN_AMOUNT, exception.apiCode());
    }

    @Test
    void rejectsAmountNotAlignedToStepSize() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> LoanAmountValidator.validateAgainstCreditLimits(
                        new BigDecimal("1550000"),
                        new BigDecimal("500000"),
                        new BigDecimal("2800000"),
                        new BigDecimal("100000")
                )
        );
        assertEquals(ApiCode.INVALID_LOAN_AMOUNT, exception.apiCode());
    }

    @Test
    void skipsCreditLimitValidationWhenAnyLimitIsMissing() {
        assertDoesNotThrow(() -> LoanAmountValidator.validateAgainstCreditLimits(
                new BigDecimal("1000000"),
                null,
                new BigDecimal("2800000"),
                new BigDecimal("100000")
        ));
        assertDoesNotThrow(() -> LoanAmountValidator.validateAgainstCreditLimits(
                new BigDecimal("1000000"),
                new BigDecimal("500000"),
                null,
                new BigDecimal("100000")
        ));
        assertDoesNotThrow(() -> LoanAmountValidator.validateAgainstCreditLimits(
                new BigDecimal("1000000"),
                new BigDecimal("500000"),
                new BigDecimal("2800000"),
                null
        ));
    }

    @Test
    void skipsCreditLimitValidationWhenStepSizeIsNotPositive() {
        assertDoesNotThrow(() -> LoanAmountValidator.validateAgainstCreditLimits(
                new BigDecimal("1000000"),
                new BigDecimal("500000"),
                new BigDecimal("2800000"),
                BigDecimal.ZERO
        ));
    }
}
