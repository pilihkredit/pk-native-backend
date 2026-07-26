package com.pk.core.home.port;

import java.util.Optional;

public interface HomeLifecycleReadRepository {
    Optional<CreditApplySnapshot> findLatestCreditApply(long userId);

    Optional<LoanApplySnapshot> findLatestLoanApply(long userId);

    int countPendingRepayLoans(long userId);

    boolean hasOverdueRepay(long userId);

    boolean hasDisbursedLoan(long userId);

    record CreditApplySnapshot(String applyId, String status) {
    }

    record LoanApplySnapshot(String loanApplyId, String status) {
    }
}
