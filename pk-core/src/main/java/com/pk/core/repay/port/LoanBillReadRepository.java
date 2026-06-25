package com.pk.core.repay.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LoanBillReadRepository {
    List<LoanBillRecord> findByProfileIdAndBillFilter(long profileId, BillFilter filter);

    Optional<LoanBillRecord> findByProfileIdAndLoanApplyId(long profileId, String loanApplyId);

    List<LoanBillRecord> findPendingByProfileId(long profileId);

    enum BillFilter {
        ACTIVE,
        SETTLED
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
