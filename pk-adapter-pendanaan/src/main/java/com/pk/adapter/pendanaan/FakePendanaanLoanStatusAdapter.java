package com.pk.adapter.pendanaan;

import com.pk.core.loan.port.LenderLoanStatusPort;

public class FakePendanaanLoanStatusAdapter implements LenderLoanStatusPort {
    @Override
    public LenderLoanStatusResult queryStatus(String loanApplyId) {
        return new LenderLoanStatusResult(
                "PROCESSING",
                "LN-FAKE-" + loanApplyId,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
