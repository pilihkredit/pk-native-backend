package com.pk.core.callback.port;

import java.math.BigDecimal;

public interface LoanCallbackParser {
    ParsedLoanCallback parse(String rawPayloadJson);

    record ParsedLoanCallback(
            String loanApplyId,
            String loanApplyNo,
            String externalStatus,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Long payTime,
            Long freezeEndTime
    ) {
    }
}
