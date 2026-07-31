package com.pk.app.profile.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.profile.application.IdentityOcrDevApplicationService;
import com.pk.app.profile.dto.request.IdentityOcrDevLenderSyncRequest;
import com.pk.app.profile.dto.response.IdentityOcrDevLenderSyncResponse;
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
 * Local/dev helper: push identity directly to lender without OCR liveness flow.
 */
@RestController
@RequestMapping("/profile/identity/ocr/dev")
@ConditionalOnProperty(prefix = "pk.ocr", name = "dev-lender-sync-enabled", havingValue = "true")
public class IdentityOcrDevController {
    private final IdentityOcrDevApplicationService identityOcrDevApplicationService;

    public IdentityOcrDevController(IdentityOcrDevApplicationService identityOcrDevApplicationService) {
        this.identityOcrDevApplicationService = identityOcrDevApplicationService;
    }

    @PostMapping("/lender-sync")
    public ApiResponse<IdentityOcrDevLenderSyncResponse> lenderSync(
            @Valid @RequestBody IdentityOcrDevLenderSyncRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                identityOcrDevApplicationService.syncToLender(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
