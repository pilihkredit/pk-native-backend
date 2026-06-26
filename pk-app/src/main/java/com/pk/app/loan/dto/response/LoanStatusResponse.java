package com.pk.app.loan.dto.response;

import java.math.BigDecimal;

public record LoanStatusResponse(
        String loanApplyId,
        String status,
        String loanApplyNo,
        String billNo,
        BigDecimal applyAmt,
        BigDecimal payAmount,
        Long payTime
) {
}
