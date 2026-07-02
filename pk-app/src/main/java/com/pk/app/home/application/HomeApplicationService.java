package com.pk.app.home.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.home.dto.response.HomeSummaryResponse;
import com.pk.app.profile.application.ProfileDeviceResolver;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.home.HomeSummaryFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class HomeApplicationService {
    private final HomeSummaryFacade homeSummaryFacade;
    private final ProfileDeviceResolver profileDeviceResolver;

    public HomeApplicationService(
            HomeSummaryFacade homeSummaryFacade,
            ProfileDeviceResolver profileDeviceResolver
    ) {
        this.homeSummaryFacade = homeSummaryFacade;
        this.profileDeviceResolver = profileDeviceResolver;
    }

    public HomeSummaryResponse getSummary(AuthenticatedPrincipal principal, HttpServletRequest httpRequest) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        HomeSummaryFacade.HomeSummaryResult result = homeSummaryFacade.getSummary(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                profileDeviceResolver.resolve(principal, ClientRequestHeaders.require(httpRequest))
        );
        return toResponse(result);
    }

    public String resolveUserStage(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return homeSummaryFacade.resolveLocalUserStage(principal.profileId(), principal.partnerUserId());
    }

    public String resolveUserStage(long profileId, String partnerUserId) {
        return homeSummaryFacade.resolveLocalUserStage(profileId, partnerUserId);
    }

    private static HomeSummaryResponse toResponse(HomeSummaryFacade.HomeSummaryResult result) {
        return new HomeSummaryResponse(
                result.partnerUserId(),
                result.userStage(),
                result.nextAction(),
                result.kycStatus(),
                result.completedModules(),
                result.missingModules(),
                result.lenderUserId(),
                result.userLoanLifeTimeStatus(),
                result.userLoanLifeTimeLastAction(),
                result.freezeEndTime(),
                result.onLoanCount(),
                result.creditContractExpireTime(),
                result.lastLenderRequestJson(),
                result.lastLenderResponseJson(),
                result.queriedAt() == null ? null : result.queriedAt().toEpochMilli()
        );
    }
}
