package com.pk.app.profile.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.dto.request.IdentityBasicSaveRequest;
import com.pk.app.profile.dto.request.IdentityOcrCheckRequest;
import com.pk.app.profile.dto.request.IdentityOcrFaceRecognitionRequest;
import com.pk.app.profile.dto.request.IdentityOcrLicenseTokenRequest;
import com.pk.app.profile.dto.request.IdentityOcrLivenessCheckRequest;
import com.pk.app.profile.dto.response.IdentityBasicSaveResponse;
import com.pk.app.profile.dto.response.IdentityOcrCheckResponse;
import com.pk.app.profile.dto.response.IdentityOcrFaceRecognitionResponse;
import com.pk.app.profile.dto.response.IdentityOcrLicenseTokenResponse;
import com.pk.app.profile.dto.response.IdentityOcrLivenessCheckResponse;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.profile.IdentityOcrFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "pk.ocr", name = "enabled", havingValue = "true")
public class IdentityOcrApplicationService {
    private final IdentityOcrFacade identityOcrFacade;
    private final PendanaanProperties pendanaanProperties;

    public IdentityOcrApplicationService(
            IdentityOcrFacade identityOcrFacade,
            PendanaanProperties pendanaanProperties
    ) {
        this.identityOcrFacade = identityOcrFacade;
        this.pendanaanProperties = pendanaanProperties;
    }

    public IdentityOcrLicenseTokenResponse getLicenseToken(
            AuthenticatedPrincipal principal,
            IdentityOcrLicenseTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        String traceId = RequestTrace.resolveTraceId(httpRequest);
        String clientRequestId = RequestTrace.resolveClientRequestId(httpRequest, null);
        IdentityOcrFacade.LicenseTokenResult result = identityOcrFacade.getLicenseToken(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                request == null ? null : request.licenseEffectiveSeconds(),
                clientRequestId,
                traceId
        );
        return new IdentityOcrLicenseTokenResponse(result.licenseToken(), result.effectiveSeconds());
    }

    public IdentityBasicSaveResponse saveBasic(
            AuthenticatedPrincipal principal,
            IdentityBasicSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        RequestTrace.resolveClientRequestId(httpRequest, request.requestId());
        IdentityOcrFacade.BasicSaveResult result = identityOcrFacade.saveBasic(
                principal.profileId(),
                principal.mobileNo(),
                new IdentityOcrFacade.BasicSaveCommand(
                        request.requestId(),
                        request.name(),
                        request.idNo()
                )
        );
        return new IdentityBasicSaveResponse(result.requestId(), result.moduleStatus());
    }

    public IdentityOcrCheckResponse ocrCheck(
            AuthenticatedPrincipal principal,
            IdentityOcrCheckRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        String traceId = RequestTrace.resolveTraceId(httpRequest);
        String clientRequestId = RequestTrace.resolveClientRequestId(httpRequest, null);
        IdentityOcrFacade.OcrCheckResult result = identityOcrFacade.ocrCheck(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                request.imageBase64(),
                clientRequestId,
                traceId
        );
        return new IdentityOcrCheckResponse(
                result.ocrName(),
                result.ocrIdNo(),
                result.gender(),
                result.religion(),
                result.maritalStatus(),
                result.birthday(),
                result.birthPlace(),
                result.address(),
                result.occupation(),
                result.nationality(),
                result.bloodType(),
                result.expiryDate(),
                result.province(),
                result.city(),
                result.district()
        );
    }

    public IdentityOcrLivenessCheckResponse livenessCheck(
            AuthenticatedPrincipal principal,
            IdentityOcrLivenessCheckRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        String traceId = RequestTrace.resolveTraceId(httpRequest);
        String clientRequestId = RequestTrace.resolveClientRequestId(httpRequest, null);
        IdentityOcrFacade.LivenessCheckResult result = identityOcrFacade.livenessCheck(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                request.livenessId(),
                clientRequestId,
                traceId
        );
        return new IdentityOcrLivenessCheckResponse(
                result.livenessScore(),
                result.passed(),
                result.threshold()
        );
    }

    public IdentityOcrFaceRecognitionResponse faceRecognition(
            AuthenticatedPrincipal principal,
            IdentityOcrFaceRecognitionRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        ClientRequestHeaders.ResolvedClientHeaders headers = ClientRequestHeaders.require(httpRequest);
        String traceId = RequestTrace.resolveTraceId(httpRequest);
        RequestTrace.resolveClientRequestId(httpRequest, request.requestId());
        IdentityOcrFacade.FaceRecognitionResult result = identityOcrFacade.faceRecognition(
                principal.profileId(),
                principal.partnerUserId(),
                principal.mobileNo(),
                new IdentityOcrFacade.FaceRecognitionCommand(
                        request.requestId(),
                        request.faceImageBase64(),
                        request.idCardImageBase64(),
                        ProfileDeviceSupport.resolveLenderDevice(request.device(), headers, pendanaanProperties)
                ),
                traceId
        );
        return new IdentityOcrFaceRecognitionResponse(
                result.requestId(),
                result.similarity(),
                result.passed(),
                result.threshold(),
                result.moduleStatus(),
                result.lenderResponse()
        );
    }

    private static void requirePrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        if (principal.profileId() <= 0) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST, "profileId is required");
        }
        if (principal.partnerUserId() == null || principal.partnerUserId().isBlank()) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST, "partnerUserId is required");
        }
        if (principal.mobileNo() == null || principal.mobileNo().isBlank()) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST, "mobileNo is required");
        }
    }
}
