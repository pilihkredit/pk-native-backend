package com.pk.core.loan;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record LoanApplication(
        String loanApplyId,
        String applyId,
        String pkCode,
        BigDecimal applyAmt,
        String quoteNo,
        LoanStatus status,
        int version,
        Instant createdAt
) {
    public LoanApplication {
        Objects.requireNonNull(loanApplyId, "loanApplyId is required");
        Objects.requireNonNull(applyId, "applyId is required");
        Objects.requireNonNull(pkCode, "pkCode is required");
        Objects.requireNonNull(applyAmt, "applyAmt is required");
        Objects.requireNonNull(quoteNo, "quoteNo is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
    }
}
