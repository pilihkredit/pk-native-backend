package com.pk.app.profile.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.app.profile.dto.request.ProfileInfoQueryRequest;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.profile.ProfileQueryFacade;
import org.springframework.stereotype.Service;

@Service
public class ProfileQueryApplicationService {
    private final ProfileQueryFacade profileQueryFacade;
    private final PendanaanProperties pendanaanProperties;

    public ProfileQueryApplicationService(
            ProfileQueryFacade profileQueryFacade,
            PendanaanProperties pendanaanProperties
    ) {
        this.profileQueryFacade = profileQueryFacade;
        this.pendanaanProperties = pendanaanProperties;
    }

    public JsonNode query(AuthenticatedPrincipal principal, ProfileInfoQueryRequest request) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        requireLenderHttp();
        if (!principal.partnerUserId().equals(request.partnerUserId())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return profileQueryFacade.query(request.partnerUserId(), request.modules());
    }

    private void requireLenderHttp() {
        if (!pendanaanProperties.httpEnabled() || !pendanaanProperties.httpCredentialsPresent()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
    }
}
