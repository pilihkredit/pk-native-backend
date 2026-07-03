package com.pk.core.loan.port;

import java.util.List;

public interface LenderLoanContractPort {
    LenderLoanContractListResult listContracts(String loanApplyId);

    record LenderLoanContract(
            String contractType,
            String contractName,
            String contractUrl
    ) {
    }

    record LenderLoanContractListResult(
            String loanApplyId,
            String loanApplyNo,
            String billNo,
            String requestJson,
            String responseDataJson,
            List<LenderLoanContract> contracts
    ) {
    }
}
