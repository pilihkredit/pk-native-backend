package com.pk.core.loan;

import java.math.BigDecimal;
import java.time.Instant;

public record LenderTrialTerm(
        int termNo,
        Instant dueDate,
        BigDecimal schdAmount,
        BigDecimal schdPrincipal,
        BigDecimal schdInterest
) {
}
