package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayTrialFacade;
import java.math.BigDecimal;

public record RepayTrialBillSummaryResponse(
        String loanApplyId,
        String billNo,
        BigDecimal repayAmount,
        String repayAmountDisplay,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal penalty,
        BigDecimal fee
) {
    public static RepayTrialBillSummaryResponse from(RepayTrialFacade.BillTrialSummary summary) {
        return new RepayTrialBillSummaryResponse(
                summary.loanApplyId(),
                summary.billNo(),
                summary.repayAmount(),
                summary.repayAmountDisplay(),
                summary.principal(),
                summary.interest(),
                summary.penalty(),
                summary.fee()
        );
    }
}
