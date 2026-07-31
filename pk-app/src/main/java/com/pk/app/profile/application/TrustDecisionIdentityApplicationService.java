package com.pk.app.profile.application;

import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.dto.request.TrustDecisionLivenessLicenseRequest;
import com.pk.app.profile.dto.request.TrustDecisionLivenessResultRequest;
import com.pk.app.profile.dto.request.TrustDecisionOcrCheckRequest;
import com.pk.app.profile.dto.response.TrustDecisionLivenessLicenseResponse;
import com.pk.app.profile.dto.response.TrustDecisionLivenessResultResponse;
import com.pk.app.profile.dto.response.TrustDecisionOcrCheckResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.profile.TrustDecisionIdentityFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class TrustDecisionIdentityApplicationService {
    private final TrustDecisionIdentityFacade facade;
    private final PendanaanProperties pendanaanProperties;

    public TrustDecisionIdentityApplicationService(
            TrustDecisionIdentityFacade facade,
            PendanaanProperties pendanaanProperties
    ) {
        this.facade = facade;
        this.pendanaanProperties = pendanaanProperties;
    }

    public TrustDecisionOcrCheckResponse ocrCheck(
            AuthenticatedPrincipal principal,
            TrustDecisionOcrCheckRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        var result = facade.ocrCheck(
                principal.userId(), principal.partnerUserId(), principal.mobileNo(), request.imageBase64(),
                RequestTrace.resolveClientRequestId(httpRequest, null), RequestTrace.resolveTraceId(httpRequest));
        var parsed = result.parsed();
        return new TrustDecisionOcrCheckResponse(
                result.result(), result.sequenceId(), parsed.ocrName(), parsed.ocrIdNo(), parsed.gender(),
                parsed.religion(), parsed.maritalStatus(), parsed.birthday(), parsed.birthPlace(), parsed.address(),
                parsed.occupation(), parsed.nationality(), parsed.bloodType(), parsed.expiryDate(), parsed.province(),
                parsed.city(), parsed.district());
    }

    public TrustDecisionLivenessLicenseResponse livenessLicense(
            AuthenticatedPrincipal principal,
            TrustDecisionLivenessLicenseRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        int duration = request.sessionDurationSeconds() == null ? 600 : request.sessionDurationSeconds();
        var result = facade.obtainLivenessLicense(
                principal.userId(), principal.partnerUserId(), principal.mobileNo(), duration,
                RequestTrace.resolveClientRequestId(httpRequest, null), RequestTrace.resolveTraceId(httpRequest));
        return new TrustDecisionLivenessLicenseResponse(
                result.license(), result.expiryTimestamp(), result.sequenceId());
    }

    public TrustDecisionLivenessResultResponse livenessResult(
            AuthenticatedPrincipal principal,
            TrustDecisionLivenessResultRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        var headers = ClientRequestHeaders.require(httpRequest);
        String traceId = RequestTrace.resolveTraceId(httpRequest);
        RequestTrace.resolveClientRequestId(httpRequest, request.requestId());
        var result = facade.completeInteractiveLiveness(
                principal.userId(), principal.partnerUserId(), principal.mobileNo(),
                request.requestId(), request.livenessId(),
                ProfileDeviceSupport.resolveLenderDevice(request.device(), headers, pendanaanProperties), traceId);
        return new TrustDecisionLivenessResultResponse(
                result.requestId(), result.result(), result.sequenceId(),
                result.moduleStatus(), result.lenderResponse());
    }

    private static void requirePrincipal(AuthenticatedPrincipal principal) {
        if (principal == null || principal.userId() <= 0) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (principal.partnerUserId() == null || principal.partnerUserId().isBlank()) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST, "partnerUserId is required");
        }
        if (principal.mobileNo() == null || principal.mobileNo().isBlank()) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST, "mobileNo is required");
        }
    }
}
