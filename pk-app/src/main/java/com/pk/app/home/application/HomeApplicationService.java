package com.pk.app.home.application;

import com.pk.app.home.dto.response.CreditApplySummaryResponse;
import com.pk.app.home.dto.response.HomeSummaryResponse;
import com.pk.app.home.dto.response.LoanApplySummaryResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.home.port.HomeLifecycleReadRepository.CreditApplySnapshot;
import com.pk.core.home.port.HomeLifecycleReadRepository.LoanApplySnapshot;
import com.pk.infra.home.HomeSummaryFacade;
import org.springframework.stereotype.Service;

@Service
public class HomeApplicationService {
    private final HomeSummaryFacade homeSummaryFacade;

    public HomeApplicationService(HomeSummaryFacade homeSummaryFacade) {
        this.homeSummaryFacade = homeSummaryFacade;
    }

    public HomeSummaryResponse getSummary(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        HomeSummaryFacade.HomeSummaryResult result = homeSummaryFacade.getSummary(
                principal.profileId(),
                principal.partnerUserId()
        );
        return new HomeSummaryResponse(
                result.partnerUserId(),
                result.userStage(),
                result.nextAction(),
                result.kycStatus(),
                toCreditApplyResponse(result.latestCreditApply()),
                toLoanApplyResponse(result.latestLoanApply()),
                result.pendingRepayBillCount(),
                result.hasOverdue()
        );
    }

    public String resolveUserStage(AuthenticatedPrincipal principal) {
        return getSummary(principal).userStage();
    }

    public String resolveUserStage(long profileId, String partnerUserId) {
        return homeSummaryFacade.getSummary(profileId, partnerUserId).userStage();
    }

    private static CreditApplySummaryResponse toCreditApplyResponse(CreditApplySnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new CreditApplySummaryResponse(snapshot.applyId(), snapshot.status());
    }

    private static LoanApplySummaryResponse toLoanApplyResponse(LoanApplySnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new LoanApplySummaryResponse(snapshot.loanApplyId(), snapshot.status());
    }
}
