package com.pk.core.repay.port;

import java.math.BigDecimal;
import java.util.List;

public interface LenderLoanBillListPort {
    LenderLoanBillListResult listBills(String partnerUserId, List<String> billStatuses);

    record LenderLoanBillListResult(
            String requestJson,
            List<LenderLoanBill> bills
    ) {
    }

    record LenderLoanBill(
            String loanApplyId,
            String loanApplyNo,
            String lenderUserId,
            String billNo,
            BigDecimal applyAmt,
            String billStatus,
            Long termDueDate,
            BigDecimal nextDueAmount,
            String responseItemJson
    ) {
    }
}
