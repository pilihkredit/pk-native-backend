package com.pk.app.ops.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.ops.application.OpsAccountClosureApplicationService;
import com.pk.app.ops.dto.OpsAccountClosureEligibilityRequest;
import com.pk.app.ops.dto.OpsAccountClosureEligibilityResponse;
import com.pk.app.ops.dto.OpsAccountClosureSubmitRequest;
import com.pk.app.ops.dto.OpsAccountClosureSubmitResponse;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer-service backoffice APIs for account closure (enqueue {@code user_deleted} + lender disable).
 */
@RestController
@RequestMapping("/ops/account-closure")
@PublicApi
public class OpsAccountClosureController {
    private final OpsAccountClosureApplicationService applicationService;

    public OpsAccountClosureController(OpsAccountClosureApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/submit")
    public ApiResponse<OpsAccountClosureSubmitResponse> submit(
            @RequestHeader(value = "X-Ops-Token", required = false) String opsToken,
            @Valid @RequestBody OpsAccountClosureSubmitRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                applicationService.submit(httpRequest, opsToken, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/eligibility-check")
    public ApiResponse<OpsAccountClosureEligibilityResponse> eligibilityCheck(
            @RequestHeader(value = "X-Ops-Token", required = false) String opsToken,
            @Valid @RequestBody OpsAccountClosureEligibilityRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                applicationService.checkEligibility(httpRequest, opsToken, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
