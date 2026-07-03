package com.pk.app.loan.dto.response;

public record LoanContractResponse(
        String contractType,
        String contractName,
        String contractUrl,
        String signStatus
) {
}
