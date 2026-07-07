package com.pk.app.loan.application;

import com.pk.app.loan.dto.request.LoanTrialRequest;
import com.pk.app.loan.dto.response.LoanTrialResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.loan.LoanTrialFacade;
import org.springframework.stereotype.Service;

@Service
public class LoanTrialApplicationService {
    private final LoanTrialFacade loanTrialFacade;

    public LoanTrialApplicationService(LoanTrialFacade loanTrialFacade) {
        this.loanTrialFacade = loanTrialFacade;
    }

    public LoanTrialResponse trial(AuthenticatedPrincipal principal, LoanTrialRequest request) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return LoanTrialResponse.from(loanTrialFacade.trial(
                principal.profileId(),
                new LoanTrialFacade.TrialCommand(
                        request.requestId(),
                        request.applyId(),
                        request.applyAmt(),
                        request.productCode(),
                        request.repayMethod(),
                        request.couponId()
                )
        ));
    }
}
