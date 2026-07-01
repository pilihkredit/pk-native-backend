package com.pk.app.profile.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.application.IdentityOcrApplicationService;
import com.pk.app.profile.dto.request.IdentityOcrCheckRequest;
import com.pk.app.profile.dto.request.IdentityOcrFaceRecognitionRequest;
import com.pk.app.profile.dto.request.IdentityOcrLicenseTokenRequest;
import com.pk.app.profile.dto.request.IdentityOcrLivenessCheckRequest;
import com.pk.app.profile.dto.response.IdentityOcrCheckResponse;
import com.pk.app.profile.dto.response.IdentityOcrFaceRecognitionResponse;
import com.pk.app.profile.dto.response.IdentityOcrLicenseTokenResponse;
import com.pk.app.profile.dto.response.IdentityOcrLivenessCheckResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile/identity/ocr")
@ConditionalOnProperty(prefix = "pk.ocr", name = "enabled", havingValue = "true")
public class IdentityOcrController {
    private final IdentityOcrApplicationService identityOcrApplicationService;

    public IdentityOcrController(IdentityOcrApplicationService identityOcrApplicationService) {
        this.identityOcrApplicationService = identityOcrApplicationService;
    }

    @PostMapping("/license-token")
    public ApiResponse<IdentityOcrLicenseTokenResponse> licenseToken(
            @RequestBody(required = false) IdentityOcrLicenseTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                identityOcrApplicationService.getLicenseToken(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/check")
    public ApiResponse<IdentityOcrCheckResponse> check(
            @Valid @RequestBody IdentityOcrCheckRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                identityOcrApplicationService.ocrCheck(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/liveness-check")
    public ApiResponse<IdentityOcrLivenessCheckResponse> livenessCheck(
            @Valid @RequestBody IdentityOcrLivenessCheckRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                identityOcrApplicationService.livenessCheck(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/face-recognition")
    public ApiResponse<IdentityOcrFaceRecognitionResponse> faceRecognition(
            @Valid @RequestBody IdentityOcrFaceRecognitionRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                identityOcrApplicationService.faceRecognition(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
