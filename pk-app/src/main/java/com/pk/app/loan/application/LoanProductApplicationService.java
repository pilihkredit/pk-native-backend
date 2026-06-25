package com.pk.app.loan.application;

import com.pk.app.loan.dto.response.LoanProductsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.loan.LoanProductFacade;
import org.springframework.stereotype.Service;

@Service
public class LoanProductApplicationService {
    private final LoanProductFacade loanProductFacade;

    public LoanProductApplicationService(LoanProductFacade loanProductFacade) {
        this.loanProductFacade = loanProductFacade;
    }

    public LoanProductsResponse listProducts(AuthenticatedPrincipal principal, String applyId) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return LoanProductsResponse.from(loanProductFacade.listProducts(principal.profileId(), applyId));
    }
}
