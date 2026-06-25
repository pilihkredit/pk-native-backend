package com.pk.core.loan.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository {
    Optional<LoanApplicationRecord> findById(long id);

    Optional<LoanApplicationRecord> findByLoanApplyId(String loanApplyId);

    Optional<LoanApplicationRecord> findByLoanApplyIdAndProfileId(String loanApplyId, long profileId);

    long insert(LoanApplicationInsert insert);

    void updateStatus(long id, String status, String externalStatus);

    void markSubmitted(long id, String externalLoanApplyNo, String externalStatus);

    void updateDisbursementDetails(
            long id,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Instant payTime
    );

    void scheduleNextPoll(long id, Instant nextPollAt);

    List<LoanApplicationRecord> findPendingPoll(int limit);

    record LoanApplicationInsert(
            String loanApplyId,
            long creditApplicationId,
            long quoteId,
            long profileId,
            long profileVersionId,
            BigDecimal applyAmt,
            String loanPurpose,
            String status
    ) {
    }

    record LoanApplicationRecord(
            long id,
            String loanApplyId,
            long creditApplicationId,
            long quoteId,
            long profileId,
            long profileVersionId,
            String externalLoanApplyNo,
            String billNo,
            String status,
            String externalStatus,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Instant payTime
    ) {
    }
}
