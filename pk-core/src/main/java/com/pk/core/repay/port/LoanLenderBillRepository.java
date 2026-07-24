package com.pk.core.repay.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface LoanLenderBillRepository {
    void upsert(LoanLenderBillData data);

    List<LoanLenderBillData> findByProfileIdAndBillStatuses(long profileId, List<String> billStatuses);

    record LoanLenderBillData(
            String loanApplyId,
            long profileId,
            String mobileNo,
            String externalLoanApplyNo,
            String lenderUserId,
            String billNo,
            BigDecimal applyAmt,
            String billStatus,
            Long termDueDate,
            BigDecimal nextDueAmount,
            Long externalInteractionId,
            Instant queriedAt
    ) {
    }
}
