package com.pk.app.home.dto.response;

/**
 * Home routing summary after login.
 */
public record HomeSummaryResponse(
        String partnerUserId,
        String userStage,
        String nextAction,
        String kycStatus,
        CreditApplySummaryResponse latestCreditApply,
        LoanApplySummaryResponse latestLoanApply,
        int pendingRepayBillCount,
        boolean hasOverdue
) {
}
