package com.pk.app.loan.application;

import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.app.loan.dto.response.LoanContractsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.loan.LoanContractFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(LoanContractFacade.class)
public class LoanContractApplicationService {
    private final LoanContractFacade loanContractFacade;
    private final PendanaanProperties pendanaanProperties;

    public LoanContractApplicationService(
            LoanContractFacade loanContractFacade,
            PendanaanProperties pendanaanProperties
    ) {
        this.loanContractFacade = loanContractFacade;
        this.pendanaanProperties = pendanaanProperties;
    }

    public LoanContractsResponse listContracts(AuthenticatedPrincipal principal, String loanApplyId) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (!pendanaanProperties.httpEnabled() || !pendanaanProperties.httpCredentialsPresent()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return LoanContractsResponse.from(
                loanContractFacade.listContracts(principal.profileId(), loanApplyId)
        );
    }
}
