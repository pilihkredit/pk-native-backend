package com.pk.app.home.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.home.dto.request.HomeSummaryRequest;
import com.pk.app.home.dto.response.HomeSummaryResponse;
import com.pk.app.profile.application.ProfileDeviceSupport;
import com.pk.adapter.pendanaan.PendanaanProperties;
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
    private final PendanaanProperties pendanaanProperties;
    private final UserDeviceWriter userDeviceWriter;

    public HomeApplicationService(
            HomeSummaryFacade homeSummaryFacade,
            PendanaanProperties pendanaanProperties,
            UserDeviceWriter userDeviceWriter
    ) {
        this.homeSummaryFacade = homeSummaryFacade;
        this.pendanaanProperties = pendanaanProperties;
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
                pendanaanProperties
        );
        userDeviceWriter.upsertFromRequest(
                principal.profileId(),
                principal.partnerUserId(),
                UUID.randomUUID().toString(),
                device
        );
        HomeSummaryFacade.HomeSummaryResult result = homeSummaryFacade.getSummary(
                principal.profileId(),
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
        return homeSummaryFacade.resolveLocalUserStage(principal.profileId(), principal.partnerUserId());
    }

    public String resolveUserStage(long profileId, String partnerUserId) {
        return homeSummaryFacade.resolveLocalUserStage(profileId, partnerUserId);
    }

    private static HomeSummaryResponse toResponse(HomeSummaryFacade.HomeSummaryResult result) {
        return new HomeSummaryResponse(
                result.partnerUserId(),
                result.userId(),
                result.userLoanLifeTimeStatus(),
                result.userLoanLifeTimeLastAction(),
                result.freezeEndTime(),
                result.onLoanCount(),
                result.creditContractExpireTime(),
                result.autoCredit()
        );
    }
}
