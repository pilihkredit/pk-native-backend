package com.pk.adapter.apipartner;

import com.pk.core.loan.port.LenderLoanApplyPort;

public class FakeApiPartnerLoanApplyAdapter implements LenderLoanApplyPort {
    @Override
    public LenderLoanApplyResult apply(LenderLoanApplyCommand command) {
        return new LenderLoanApplyResult(
                command.loanApplyId(),
                "LN-FAKE-" + command.loanApplyId(),
                "USR-FAKE",
                "PROCESSING",
                1L
        );
    }
}
