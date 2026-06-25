package com.pk.app.loan.dto.response;

import com.pk.infra.loan.LoanTrialFacade;
import java.math.BigDecimal;
import java.util.List;

public record LoanTrialResponse(
        String quoteNo,
        long expiresAt,
        BigDecimal applyAmt,
        BigDecimal payAmount,
        BigDecimal schdAmount,
        BigDecimal interest,
        Integer loanTerm,
        List<LoanTrialTermResponse> termInfo
) {
    public static LoanTrialResponse from(LoanTrialFacade.TrialResult result) {
        return new LoanTrialResponse(
                result.quoteNo(),
                result.expiresAt(),
                result.applyAmt(),
                result.payAmount(),
                result.schdAmount(),
                result.interest(),
                result.loanTerm(),
                result.termInfo().stream().map(LoanTrialTermResponse::from).toList()
        );
    }
}
