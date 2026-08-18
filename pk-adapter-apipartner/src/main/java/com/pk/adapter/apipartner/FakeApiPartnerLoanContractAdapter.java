package com.pk.adapter.apipartner;

import com.pk.core.loan.port.LenderLoanContractPort;
import java.util.List;

public class FakeApiPartnerLoanContractAdapter implements LenderLoanContractPort {
    @Override
    public LenderLoanContractListResult listContracts(String loanApplyId) {
        return new LenderLoanContractListResult(loanApplyId, null, null, null, List.of());
    }
}
