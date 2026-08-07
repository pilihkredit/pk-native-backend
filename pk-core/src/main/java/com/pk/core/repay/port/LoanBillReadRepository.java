package com.pk.core.repay.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoanBillReadRepository {
    List<LoanBillRecord> findByUserIdAndBillFilter(long userId, BillFilter filter);

    Optional<LoanBillRecord> findByUserIdAndLoanApplyId(long userId, String loanApplyId);

    List<LoanBillRecord> findPendingByUserId(long userId);

    /**
     * Loans with unpaid/overdue plan terms. Cursor is {@code loan_application.id} ascending.
     * Lookback uses {@code loan_application.created_at} when provided.
     */
    List<TrialBackfillCandidate> findDueForTrialBackfill(
            long afterLoanApplicationId,
            int limit,
            Instant createdFromInclusive
    );

    enum BillFilter {
        ACTIVE,
        SETTLED
    }

    record TrialBackfillCandidate(
            long loanApplicationId,
            long userId,
            String loanApplyId
    ) {
    }

    record LoanBillRecord(
            long loanApplicationId,
            String loanApplyId,
            String loanApplyNo,
            String billNo,
            BigDecimal applyAmt,
            String loanStatus
    ) {
    }
}
