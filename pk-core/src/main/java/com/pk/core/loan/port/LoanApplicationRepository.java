package com.pk.core.loan.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository {
    Optional<LoanApplicationRecord> findById(long id);

    Optional<LoanApplicationRecord> findByRequestId(String requestId);

    Optional<LoanApplicationRecord> findByLoanApplyId(String loanApplyId);

    Optional<LoanApplicationRecord> findByLoanApplyIdAndProfileId(String loanApplyId, long profileId);

    long insert(LoanApplicationInsert insert);

    void updateStatus(long id, String status, String externalStatus);

    void markSubmitted(long id, String externalLoanApplyNo, String externalStatus);

    void markLenderApplySubmitted(LenderApplySubmitted submitted);

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
            String requestId,
            String applyId,
            String mobileNo,
            long creditApplicationId,
            Long quoteId,
            String quoteNo,
            long profileId,
            long profileVersionId,
            BigDecimal applyAmt,
            String productCode,
            String repayMethod,
            Long couponId,
            String loanPurpose,
            BigDecimal lat,
            BigDecimal lng,
            String ip,
            String address,
            String adId,
            String status
    ) {
    }

    record LenderApplySubmitted(
            long id,
            String externalLoanApplyNo,
            String lenderUserId,
            String externalStatus,
            Long externalInteractionId
    ) {
    }

    record LoanApplicationRecord(
            long id,
            String loanApplyId,
            String requestId,
            String applyId,
            String mobileNo,
            long creditApplicationId,
            Long quoteId,
            String quoteNo,
            long profileId,
            long profileVersionId,
            String externalLoanApplyNo,
            String lenderUserId,
            String billNo,
            String status,
            String externalStatus,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Instant payTime
    ) {
    }
}
