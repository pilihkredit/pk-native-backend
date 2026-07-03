package com.pk.app.loan.dto.response;

import com.pk.infra.loan.LoanContractFacade;
import java.util.List;

public record LoanContractsResponse(String loanApplyId, List<LoanContractResponse> contracts) {
    public static LoanContractsResponse from(LoanContractFacade.ContractsResult result) {
        return new LoanContractsResponse(
                result.loanApplyId(),
                result.contracts().stream()
                        .map(contract -> new LoanContractResponse(
                                contract.contractType(),
                                contract.contractName(),
                                contract.contractUrl(),
                                contract.signStatus()
                        ))
                        .toList()
        );
    }
}
