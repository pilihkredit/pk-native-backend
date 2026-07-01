package com.pk.app.profile.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.profile.dto.request.IdentityOcrCheckRequest;
import com.pk.app.profile.dto.request.IdentityOcrFaceRecognitionRequest;
import com.pk.app.profile.dto.request.IdentityOcrLicenseTokenRequest;
import com.pk.app.profile.dto.request.IdentityOcrLivenessCheckRequest;
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
import org.springframework.transaction.annotation.Transactional;

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
            IdentityOcrLicenseTokenRequest request
    ) {
        requirePrincipal(principal);
        IdentityOcrFacade.LicenseTokenResult result = identityOcrFacade.getLicenseToken(
                principal.profileId(),
                request == null ? null : request.licenseEffectiveSeconds()
        );
        return new IdentityOcrLicenseTokenResponse(result.licenseToken(), result.effectiveSeconds());
    }

    public IdentityOcrCheckResponse ocrCheck(
            AuthenticatedPrincipal principal,
            IdentityOcrCheckRequest request
    ) {
        requirePrincipal(principal);
        IdentityOcrFacade.OcrCheckResult result = identityOcrFacade.ocrCheck(
                principal.profileId(),
                request.imageBase64()
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
            IdentityOcrLivenessCheckRequest request
    ) {
        requirePrincipal(principal);
        IdentityOcrFacade.LivenessCheckResult result = identityOcrFacade.livenessCheck(
                principal.profileId(),
                request.livenessId()
        );
        return new IdentityOcrLivenessCheckResponse(
                result.livenessScore(),
                result.passed(),
                result.threshold()
        );
    }

    @Transactional
    public IdentityOcrFaceRecognitionResponse faceRecognition(
            AuthenticatedPrincipal principal,
            IdentityOcrFaceRecognitionRequest request,
            HttpServletRequest httpRequest
    ) {
        requirePrincipal(principal);
        ClientRequestHeaders.ResolvedClientHeaders headers = ClientRequestHeaders.require(httpRequest);
        IdentityOcrFacade.FaceRecognitionResult result = identityOcrFacade.faceRecognition(
                principal.profileId(),
                principal.partnerUserId(),
                new IdentityOcrFacade.FaceRecognitionCommand(
                        request.requestId(),
                        request.faceImageBase64(),
                        request.idCardImageBase64(),
                        ProfileDeviceSupport.resolveLenderDevice(request.device(), headers, pendanaanProperties)
                )
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
    }
}
