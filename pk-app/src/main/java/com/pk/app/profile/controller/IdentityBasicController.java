package com.pk.app.profile.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.application.IdentityOcrApplicationService;
import com.pk.app.profile.dto.request.IdentityBasicSaveRequest;
import com.pk.app.profile.dto.response.IdentityBasicSaveResponse;
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

/**
 * Manual identity name / idNo persistence (before OCR face recognition).
 */
@RestController
@RequestMapping("/profile/identity")
@ConditionalOnProperty(prefix = "pk.ocr", name = "enabled", havingValue = "true")
public class IdentityBasicController {
    private final IdentityOcrApplicationService identityOcrApplicationService;

    public IdentityBasicController(IdentityOcrApplicationService identityOcrApplicationService) {
        this.identityOcrApplicationService = identityOcrApplicationService;
    }

    /** Save legal name and EKTP locally only (no device, no lender sync). */
    @PostMapping("/basic")
    public ApiResponse<IdentityBasicSaveResponse> saveBasic(
            @Valid @RequestBody IdentityBasicSaveRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                identityOcrApplicationService.saveBasic(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
