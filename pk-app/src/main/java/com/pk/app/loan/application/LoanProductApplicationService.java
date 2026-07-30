package com.pk.app.loan.application;

import com.pk.app.loan.dto.response.LoanProductsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.review.ReviewSandboxConfigPort;
import com.pk.infra.loan.LoanProductFacade;
import org.springframework.stereotype.Service;

@Service
public class LoanProductApplicationService {
    private final LoanProductFacade loanProductFacade;
    private final ReviewSandboxConfigPort reviewSandboxConfigPort;

    public LoanProductApplicationService(
            LoanProductFacade loanProductFacade,
            ReviewSandboxConfigPort reviewSandboxConfigPort
    ) {
        this.loanProductFacade = loanProductFacade;
        this.reviewSandboxConfigPort = reviewSandboxConfigPort;
    }

    public LoanProductsResponse listProducts(AuthenticatedPrincipal principal, String applyId) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        boolean reviewUser = reviewSandboxConfigPort.findEnabledScenario(principal.mobileNo()).isPresent();
        LoanProductFacade.ProductsResult result = reviewUser
                ? loanProductFacade.listProductsForceRefresh(principal.userId(), applyId)
                : loanProductFacade.listProducts(principal.userId(), applyId);
        return LoanProductsResponse.from(result);
    }
}
