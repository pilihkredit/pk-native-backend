package com.pk.app.repay.dto.response;

import com.pk.infra.repay.RepayPlanFacade;
import java.math.BigDecimal;

public record RepayPlanTermResponse(
        int termNo,
        String termNoDisplay,
        Long dueDate,
        String dueDateDisplay,
        String termStatus,
        BigDecimal shouldAmount,
        String shouldAmountDisplay,
        BigDecimal shouldPrincipal,
        BigDecimal shouldInterest,
        Integer overdueDays
) {
    public static RepayPlanTermResponse from(RepayPlanFacade.TermResult term) {
        return new RepayPlanTermResponse(
                term.termNo(),
                term.termNoDisplay(),
                term.dueDate(),
                term.dueDateDisplay(),
                term.termStatus(),
                term.shouldAmount(),
                term.shouldAmountDisplay(),
                term.shouldPrincipal(),
                term.shouldInterest(),
                term.overdueDays()
        );
    }
}
