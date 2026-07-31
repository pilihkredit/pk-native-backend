package com.pk.core.loan.port;

public interface LoanStatusHistoryRepository {
    void insert(
            long loanApplicationId,
            String fromStatus,
            String toStatus,
            String externalStatus,
            String source
    );
}
