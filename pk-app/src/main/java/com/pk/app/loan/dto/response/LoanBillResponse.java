package com.pk.app.loan.dto.response;

import com.pk.infra.repay.LoanBillsFacade;
import java.math.BigDecimal;

public record LoanBillResponse(
        String loanApplyId,
        String loanApplyNo,
        String billNo,
        BigDecimal applyAmt,
        String applyAmtDisplay,
        String billStatus,
        Long nextDueDate,
        String nextDueDateDisplay,
        BigDecimal nextDueAmount,
        String nextDueAmountDisplay,
        boolean canReloan
) {
    public static LoanBillResponse from(LoanBillsFacade.BillResult bill) {
        return new LoanBillResponse(
                bill.loanApplyId(),
                bill.loanApplyNo(),
                bill.billNo(),
                bill.applyAmt(),
                bill.applyAmtDisplay(),
                bill.billStatus(),
                bill.nextDueDate(),
                bill.nextDueDateDisplay(),
                bill.nextDueAmount(),
                bill.nextDueAmountDisplay(),
                bill.canReloan()
        );
    }
}
