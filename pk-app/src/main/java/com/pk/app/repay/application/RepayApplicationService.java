package com.pk.app.repay.application;

import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.app.repay.dto.request.RepayCurrentOrderRequest;
import com.pk.app.repay.dto.request.RepayTrialBatchRequest;
import com.pk.app.repay.dto.request.RepayTrialRequest;
import com.pk.app.repay.dto.request.RepayVaDefaultRequest;
import com.pk.app.repay.dto.response.RepayCurrentOrderResponse;
import com.pk.app.repay.dto.response.RepayPlanListResponse;
import com.pk.app.repay.dto.response.RepayPlanResponse;
import com.pk.app.repay.dto.response.RepayTrialBatchResponse;
import com.pk.app.repay.dto.response.RepayTrialResponse;
import com.pk.app.repay.dto.response.RepayVaDefaultResponse;
import com.pk.app.repay.dto.response.RepayVaListResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.repay.RepayCurrentOrderFacade;
import com.pk.infra.repay.RepayPlanFacade;
import com.pk.infra.repay.RepayTrialFacade;
import com.pk.infra.repay.RepayVaFacade;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RepayApplicationService {
    private final RepayPlanFacade repayPlanFacade;
    private final RepayVaFacade repayVaFacade;
    private final RepayTrialFacade repayTrialFacade;
    private final RepayCurrentOrderFacade repayCurrentOrderFacade;
    private final PendanaanProperties pendanaanProperties;

    public RepayApplicationService(
            RepayPlanFacade repayPlanFacade,
            RepayVaFacade repayVaFacade,
            RepayTrialFacade repayTrialFacade,
            RepayCurrentOrderFacade repayCurrentOrderFacade,
            PendanaanProperties pendanaanProperties
    ) {
        this.repayPlanFacade = repayPlanFacade;
        this.repayVaFacade = repayVaFacade;
        this.repayTrialFacade = repayTrialFacade;
        this.repayCurrentOrderFacade = repayCurrentOrderFacade;
        this.pendanaanProperties = pendanaanProperties;
    }

    public RepayPlanListResponse getPlan(AuthenticatedPrincipal principal, String loanApplyId) {
        requirePrincipal(principal);
        requireLenderHttp();
        List<RepayPlanFacade.PlanResult> plans = repayPlanFacade.getPlan(principal.profileId(), loanApplyId);
        if (loanApplyId != null && !loanApplyId.isBlank() && plans.size() == 1) {
            return RepayPlanListResponse.single(RepayPlanResponse.from(plans.getFirst()));
        }
        return RepayPlanListResponse.multiple(plans.stream().map(RepayPlanResponse::from).toList());
    }

    public RepayVaListResponse listVas(AuthenticatedPrincipal principal) {
        requirePrincipal(principal);
        requireLenderHttp();
        return RepayVaListResponse.from(repayVaFacade.listVas(
                principal.profileId(),
                principal.partnerUserId()
        ));
    }

    public RepayVaDefaultResponse setDefaultVa(AuthenticatedPrincipal principal, RepayVaDefaultRequest request) {
        requirePrincipal(principal);
        requireLenderHttp();
        return RepayVaDefaultResponse.from(repayVaFacade.setDefaultVa(
                principal.profileId(),
                principal.partnerUserId(),
                new RepayVaFacade.VaDefaultCommand(request.vaNo(), request.bankChannel())
        ));
    }

    public RepayTrialResponse trial(AuthenticatedPrincipal principal, RepayTrialRequest request) {
        requirePrincipal(principal);
        requireLenderHttp();
        return RepayTrialResponse.from(repayTrialFacade.trial(
                principal.profileId(),
                new RepayTrialFacade.TrialCommand(
                        request.requestId(),
                        request.loanApplyId(),
                        request.termNos(),
                        request.repayType()
                )
        ));
    }

    public RepayTrialBatchResponse trialBatch(AuthenticatedPrincipal principal, RepayTrialBatchRequest request) {
        requirePrincipal(principal);
        requireLenderHttp();
        return RepayTrialBatchResponse.from(repayTrialFacade.trialBatch(
                principal.profileId(),
                new RepayTrialFacade.BatchTrialCommand(
                        request.requestId(),
                        request.repayOrders().stream()
                                .map(item -> new RepayTrialFacade.BatchOrderCommand(
                                        item.loanApplyId(),
                                        item.settle(),
                                        item.termNos()
                                ))
                                .toList()
                )
        ));
    }

    public RepayCurrentOrderResponse setCurrentOrder(
            AuthenticatedPrincipal principal,
            RepayCurrentOrderRequest request
    ) {
        requirePrincipal(principal);
        requireLenderHttp();
        return RepayCurrentOrderResponse.from(repayCurrentOrderFacade.setCurrentOrder(
                principal.profileId(),
                principal.partnerUserId(),
                new RepayCurrentOrderFacade.CurrentOrderCommand(
                        request.requestId(),
                        request.batchTrialNo(),
                        request.repayOrders().stream()
                                .map(item -> new RepayCurrentOrderFacade.RepayOrderCommand(
                                        item.loanApplyId(),
                                        item.termNos()
                                ))
                                .toList()
                )
        ));
    }

    private static void requirePrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }

    private void requireLenderHttp() {
        if (!pendanaanProperties.httpEnabled() || !pendanaanProperties.httpCredentialsPresent()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
