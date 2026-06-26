package com.pk.app.repay.application;

import com.pk.app.repay.dto.response.RepayBillsOverviewResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.repay.RepayBillsOverviewFacade;
import org.springframework.stereotype.Service;

@Service
public class RepayBillsOverviewApplicationService {
    private final RepayBillsOverviewFacade repayBillsOverviewFacade;

    public RepayBillsOverviewApplicationService(RepayBillsOverviewFacade repayBillsOverviewFacade) {
        this.repayBillsOverviewFacade = repayBillsOverviewFacade;
    }

    public RepayBillsOverviewResponse billsOverview(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return RepayBillsOverviewResponse.from(repayBillsOverviewFacade.getOverview(principal.profileId()));
    }
}
