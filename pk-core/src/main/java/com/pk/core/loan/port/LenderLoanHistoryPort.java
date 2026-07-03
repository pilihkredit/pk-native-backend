package com.pk.core.loan.port;

import java.math.BigDecimal;
import java.util.List;

public interface LenderLoanHistoryPort {
    LenderLoanHistoryResult queryHistory(String partnerUserId);

    record LenderLoanHistoryResult(
            String requestJson,
            List<LenderLoanHistoryOrder> orders
    ) {
    }

    record LenderLoanHistoryOrder(
            String loanApplyId,
            String loanApplyNo,
            String lenderUserId,
            String applyStatus,
            String billNo,
            BigDecimal applyAmt,
            BigDecimal payAmount,
            Long payTime,
            Long freezeEndTime,
            Long createTime,
            String responseItemJson
    ) {
    }
}
