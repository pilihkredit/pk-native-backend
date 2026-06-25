package com.pk.app.loan.dto.response;

import com.pk.infra.loan.LoanTrialFacade;
import java.math.BigDecimal;

public record LoanTrialTermResponse(
        int termNo,
        String termNoDisplay,
        Long dueDate,
        String dueDateDisplay,
        BigDecimal schdAmount,
        String schdAmountDisplay,
        BigDecimal schdPrincipal,
        String schdPrincipalDisplay,
        BigDecimal schdInterest,
        String schdInterestDisplay
) {
    public static LoanTrialTermResponse from(LoanTrialFacade.TermResult term) {
        return new LoanTrialTermResponse(
                term.termNo(),
                term.termNoDisplay(),
                term.dueDate(),
                term.dueDateDisplay(),
                term.schdAmount(),
                term.schdAmountDisplay(),
                term.schdPrincipal(),
                term.schdPrincipalDisplay(),
                term.schdInterest(),
                term.schdInterestDisplay()
        );
    }
}
