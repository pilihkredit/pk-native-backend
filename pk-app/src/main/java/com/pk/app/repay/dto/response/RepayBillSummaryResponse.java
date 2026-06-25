package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayBillsOverviewFacade;
import java.math.BigDecimal;

public record RepayBillSummaryResponse(
        String loanApplyId,
        String billNo,
        int overdueDays,
        BigDecimal currentDueAmount,
        String currentDueAmountDisplay,
        boolean selected
) {
    public static RepayBillSummaryResponse from(RepayBillsOverviewFacade.BillSummaryResult summary) {
        return new RepayBillSummaryResponse(
                summary.loanApplyId(),
                summary.billNo(),
                summary.overdueDays(),
                summary.currentDueAmount(),
                summary.currentDueAmountDisplay(),
                summary.selected()
        );
    }
}
