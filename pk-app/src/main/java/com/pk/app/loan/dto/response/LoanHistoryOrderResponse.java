package com.pk.app.loan.dto.response;

import com.pk.infra.loan.LoanHistoryFacade;
import java.math.BigDecimal;

public record LoanHistoryOrderResponse(
        String loanApplyId,
        String loanApplyNo,
        String applyStatus,
        String billNo,
        BigDecimal applyAmt,
        BigDecimal payAmount,
        Long payTime,
        Long freezeEndTime,
        Long createTime
) {
    public static LoanHistoryOrderResponse from(LoanHistoryFacade.HistoryOrder order) {
        return new LoanHistoryOrderResponse(
                order.loanApplyId(),
                order.loanApplyNo(),
                order.externalStatus(),
                order.billNo(),
                order.applyAmt(),
                order.payAmount(),
                order.payTime(),
                order.freezeEndTime(),
                order.createTime()
        );
    }
}
