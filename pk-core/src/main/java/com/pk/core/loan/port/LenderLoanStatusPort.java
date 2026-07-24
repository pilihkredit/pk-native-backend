package com.pk.core.loan.port;

import java.math.BigDecimal;

public interface LenderLoanStatusPort {
    LenderLoanStatusResult queryStatus(String loanApplyId);

    record LenderLoanStatusResult(
            String externalStatus,
            String loanApplyNo,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Long payTime,
            Long freezeEndTime,
            Long externalInteractionId
    ) {
    }
}
