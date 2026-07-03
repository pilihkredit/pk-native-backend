package com.pk.adapter.pendanaan;

import com.pk.core.loan.port.LenderLoanContractPort;
import java.util.List;

public class FakePendanaanLoanContractAdapter implements LenderLoanContractPort {
    @Override
    public LenderLoanContractListResult listContracts(String loanApplyId) {
        return new LenderLoanContractListResult(loanApplyId, null, null, null, null, List.of());
    }
}
