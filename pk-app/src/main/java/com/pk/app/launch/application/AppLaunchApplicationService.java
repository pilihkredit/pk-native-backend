package com.pk.app.launch.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.launch.dto.request.AppLaunchRecordRequest;
import com.pk.app.launch.dto.response.AppLaunchRecordResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.launch.AppLaunchRecord;
import com.pk.infra.launch.AppLaunchFacade;
import java.time.Instant;

public class AppLaunchApplicationService {
    private final AppLaunchFacade appLaunchFacade;

    public AppLaunchApplicationService(AppLaunchFacade appLaunchFacade) {
        this.appLaunchFacade = appLaunchFacade;
    }

    public AppLaunchRecordResponse record(
            AuthenticatedPrincipal principal,
            AppLaunchRecordRequest request,
            ClientRequestHeaders.ResolvedClientHeaders headers
    ) {
        Instant occurredAt = Instant.now();
        Instant clientStartedAt = toInstant(request.clientStartedAt());
        Long userId = principal == null ? null : principal.userId();
        boolean recorded = appLaunchFacade.record(new AppLaunchRecord(
                request.launchId().trim(),
                headers.deviceNo(),
                userId,
                headers.appVersion(),
                headers.platform(),
                headers.appPackage(),
                clientStartedAt,
                occurredAt
        ));
        return new AppLaunchRecordResponse(recorded);
    }

    private static Instant toInstant(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        try {
            return Instant.ofEpochMilli(epochMillis);
        } catch (RuntimeException exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }
}
