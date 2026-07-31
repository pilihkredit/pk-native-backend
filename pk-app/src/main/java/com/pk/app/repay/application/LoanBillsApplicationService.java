package com.pk.app.repay.application;

import com.pk.app.loan.dto.response.LoanBillResponse;
import com.pk.app.loan.dto.response.LoanBillsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.repay.LoanBillsFacade;
import org.springframework.stereotype.Service;

@Service
public class LoanBillsApplicationService {
    private final LoanBillsFacade loanBillsFacade;

    public LoanBillsApplicationService(LoanBillsFacade loanBillsFacade) {
        this.loanBillsFacade = loanBillsFacade;
    }

    public LoanBillsResponse listBills(AuthenticatedPrincipal principal, String status) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        LoanBillsFacade.BillsResult result = loanBillsFacade.listBills(
                principal.userId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                status
        );
        return new LoanBillsResponse(result.bills().stream().map(LoanBillResponse::from).toList());
    }
}
