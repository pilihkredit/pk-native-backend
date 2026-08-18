package com.pk.app.home.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.home.dto.request.HomeSummaryRequest;
import com.pk.app.home.dto.response.HomeSummaryResponse;
import com.pk.app.profile.application.ProfileDeviceSupport;
import com.pk.adapter.apipartner.ApiPartnerProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.home.HomeSummaryFacade;
import com.pk.infra.profile.UserDeviceWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class HomeApplicationService {
    private final HomeSummaryFacade homeSummaryFacade;
    private final ApiPartnerProperties apiPartnerProperties;
    private final UserDeviceWriter userDeviceWriter;

    public HomeApplicationService(
            HomeSummaryFacade homeSummaryFacade,
            ApiPartnerProperties apiPartnerProperties,
            UserDeviceWriter userDeviceWriter
    ) {
        this.homeSummaryFacade = homeSummaryFacade;
        this.apiPartnerProperties = apiPartnerProperties;
        this.userDeviceWriter = userDeviceWriter;
    }

    public HomeSummaryResponse getSummary(
            AuthenticatedPrincipal principal,
            HomeSummaryRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        LenderDeviceContext device = ProfileDeviceSupport.resolveLenderDevice(
                request.device(),
                ClientRequestHeaders.require(httpRequest),
                apiPartnerProperties
        );
        userDeviceWriter.upsertFromRequest(
                principal.userId(),
                principal.partnerUserId(),
                UUID.randomUUID().toString(),
                device
        );
        HomeSummaryFacade.HomeSummaryResult result = homeSummaryFacade.getSummary(
                principal.userId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                device
        );
        return toResponse(result);
    }

    public String resolveUserStage(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return homeSummaryFacade.resolveLocalUserStage(principal.userId(), principal.partnerUserId());
    }

    public String resolveUserStage(long userId, String partnerUserId) {
        return homeSummaryFacade.resolveLocalUserStage(userId, partnerUserId);
    }

    private static HomeSummaryResponse toResponse(HomeSummaryFacade.HomeSummaryResult result) {
        return new HomeSummaryResponse(
                result.partnerUserId(),
                result.userId(),
                result.userLoanLifeTimeStatus(),
                result.userLoanLifeTimeLastAction(),
                result.freezeEndTime(),
                result.firstLoan(),
                result.firstCreditApply(),
                result.firstLoanApply(),
                result.onLoanCount(),
                result.creditContractExpireTime(),
                result.autoCredit()
        );
    }
}
