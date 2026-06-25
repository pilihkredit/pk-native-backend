package com.pk.core.home.port;

import java.util.Optional;

public interface HomeLifecycleReadRepository {
    Optional<CreditApplySnapshot> findLatestCreditApply(long profileId);

    Optional<LoanApplySnapshot> findLatestLoanApply(long profileId);

    int countPendingRepayLoans(long profileId);

    boolean hasOverdueRepay(long profileId);

    boolean hasDisbursedLoan(long profileId);

    record CreditApplySnapshot(String applyId, String status) {
    }

    record LoanApplySnapshot(String loanApplyId, String status) {
    }
}
