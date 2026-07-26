package com.pk.app.loan.application;

import com.pk.app.loan.dto.response.LoanHistoryOrderResponse;
import com.pk.app.loan.dto.response.LoanHistoryOrdersResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.loan.LoanHistoryFacade;
import org.springframework.stereotype.Service;

@Service
public class LoanHistoryApplicationService {
    private final LoanHistoryFacade loanHistoryFacade;

    public LoanHistoryApplicationService(LoanHistoryFacade loanHistoryFacade) {
        this.loanHistoryFacade = loanHistoryFacade;
    }

    public LoanHistoryOrdersResponse listOrders(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        LoanHistoryFacade.HistoryResult result = loanHistoryFacade.listHistory(
                principal.userId(),
                principal.partnerUserId(),
                principal.mobileNo()
        );
        return new LoanHistoryOrdersResponse(
                result.orders().stream().map(LoanHistoryOrderResponse::from).toList()
        );
    }
}
