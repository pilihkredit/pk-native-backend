package com.pk.app.loan.application;

import com.pk.adapter.apipartner.ApiPartnerProperties;
import com.pk.app.loan.dto.response.LoanContractsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.loan.LoanContractFacade;
import org.springframework.stereotype.Service;

@Service
public class LoanContractApplicationService {
    private final LoanContractFacade loanContractFacade;
    private final ApiPartnerProperties apiPartnerProperties;

    public LoanContractApplicationService(
            LoanContractFacade loanContractFacade,
            ApiPartnerProperties apiPartnerProperties
    ) {
        this.loanContractFacade = loanContractFacade;
        this.apiPartnerProperties = apiPartnerProperties;
    }

    public LoanContractsResponse listContracts(AuthenticatedPrincipal principal, String loanApplyId) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (!apiPartnerProperties.httpEnabled() || !apiPartnerProperties.httpCredentialsPresent()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return LoanContractsResponse.from(
                loanContractFacade.listContracts(principal.userId(), loanApplyId)
        );
    }
}
