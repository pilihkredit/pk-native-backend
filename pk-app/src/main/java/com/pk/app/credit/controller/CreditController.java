package com.pk.app.credit.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.credit.application.CreditApplicationService;
import com.pk.app.credit.dto.request.CreditApplyRequest;
import com.pk.app.credit.dto.response.CreditApplyResponse;
import com.pk.app.credit.dto.response.CreditLimitDisplayResponse;
import com.pk.app.credit.dto.response.CreditStatusResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/credit")
public class CreditController {
    private final CreditApplicationService creditApplicationService;

    public CreditController(CreditApplicationService creditApplicationService) {
        this.creditApplicationService = creditApplicationService;
    }

    @PostMapping("/apply")
    public ApiResponse<CreditApplyResponse> apply(
            @Valid @RequestBody CreditApplyRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                creditApplicationService.apply(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/limit-display")
    public ApiResponse<CreditLimitDisplayResponse> getLimitDisplay(
            @RequestParam("applyId") String applyId,
            @RequestParam(value = "repayMethod", required = false) String repayMethod,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                creditApplicationService.getLimitDisplay(principal, applyId, repayMethod),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/{applyId}/status")
    public ApiResponse<CreditStatusResponse> getStatus(
            @PathVariable("applyId") String applyId,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                creditApplicationService.getStatus(principal, applyId),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
