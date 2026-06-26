package com.pk.app.loan.dto.response;

public record LoanContractResponse(
        String contractNo,
        String contractName,
        String contractUrl,
        String signStatus
) {
}
