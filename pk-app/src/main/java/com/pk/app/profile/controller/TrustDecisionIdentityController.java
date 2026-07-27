package com.pk.app.profile.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.application.TrustDecisionIdentityApplicationService;
import com.pk.app.profile.dto.request.TrustDecisionFaceRecognitionRequest;
import com.pk.app.profile.dto.request.TrustDecisionLivenessCheckRequest;
import com.pk.app.profile.dto.request.TrustDecisionOcrCheckRequest;
import com.pk.app.profile.dto.response.TrustDecisionFaceRecognitionResponse;
import com.pk.app.profile.dto.response.TrustDecisionLivenessCheckResponse;
import com.pk.app.profile.dto.response.TrustDecisionOcrCheckResponse;
import com.pk.app.security.SecurityContextSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile/identity/tongdun")
public class TrustDecisionIdentityController {
    private final TrustDecisionIdentityApplicationService applicationService;

    public TrustDecisionIdentityController(TrustDecisionIdentityApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/ocr-check")
    public ApiResponse<TrustDecisionOcrCheckResponse> ocrCheck(
            @Valid @RequestBody TrustDecisionOcrCheckRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                applicationService.ocrCheck(SecurityContextSupport.requirePrincipal(), request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest));
    }

    @PostMapping("/liveness-check")
    public ApiResponse<TrustDecisionLivenessCheckResponse> livenessCheck(
            @Valid @RequestBody TrustDecisionLivenessCheckRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                applicationService.livenessCheck(SecurityContextSupport.requirePrincipal(), request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest));
    }

    @PostMapping("/face-recognition")
    public ApiResponse<TrustDecisionFaceRecognitionResponse> faceRecognition(
            @Valid @RequestBody TrustDecisionFaceRecognitionRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                applicationService.faceRecognition(SecurityContextSupport.requirePrincipal(), request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest));
    }
}
