package com.pk.core.repay.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LoanBillReadRepository {
    List<LoanBillRecord> findByUserIdAndBillFilter(long userId, BillFilter filter);

    Optional<LoanBillRecord> findByUserIdAndLoanApplyId(long userId, String loanApplyId);

    List<LoanBillRecord> findPendingByUserId(long userId);

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
