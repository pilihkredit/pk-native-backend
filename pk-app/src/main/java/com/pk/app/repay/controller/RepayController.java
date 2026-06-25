package com.pk.app.repay.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.repay.application.RepayApplicationService;
import com.pk.app.repay.dto.request.RepayCurrentOrderRequest;
import com.pk.app.repay.dto.request.RepayTrialBatchRequest;
import com.pk.app.repay.dto.request.RepayTrialRequest;
import com.pk.app.repay.dto.request.RepayVaDefaultRequest;
import com.pk.app.repay.dto.response.RepayBillsOverviewResponse;
import com.pk.app.repay.dto.response.RepayCurrentOrderResponse;
import com.pk.app.repay.dto.response.RepayPlanListResponse;
import com.pk.app.repay.dto.response.RepayTrialBatchResponse;
import com.pk.app.repay.dto.response.RepayTrialResponse;
import com.pk.app.repay.dto.response.RepayVaDefaultResponse;
import com.pk.app.repay.dto.response.RepayVaListResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repay")
public class RepayController {
    private final RepayApplicationService repayApplicationService;

    public RepayController(RepayApplicationService repayApplicationService) {
        this.repayApplicationService = repayApplicationService;
    }

    @GetMapping("/bills-overview")
    public ApiResponse<RepayBillsOverviewResponse> billsOverview(HttpServletRequest httpRequest) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                repayApplicationService.billsOverview(principal),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/plan")
    public ApiResponse<RepayPlanListResponse> getPlan(
            @RequestParam(value = "loanApplyId", required = false) String loanApplyId,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                repayApplicationService.getPlan(principal, loanApplyId),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/va/list")
    public ApiResponse<RepayVaListResponse> listVas(HttpServletRequest httpRequest) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                repayApplicationService.listVas(principal),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/va/default")
    public ApiResponse<RepayVaDefaultResponse> setDefaultVa(
            @Valid @RequestBody RepayVaDefaultRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                repayApplicationService.setDefaultVa(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/trial")
    public ApiResponse<RepayTrialResponse> trial(
            @Valid @RequestBody RepayTrialRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                repayApplicationService.trial(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/trial/batch")
    public ApiResponse<RepayTrialBatchResponse> trialBatch(
            @Valid @RequestBody RepayTrialBatchRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                repayApplicationService.trialBatch(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/current-order")
    public ApiResponse<RepayCurrentOrderResponse> setCurrentOrder(
            @Valid @RequestBody RepayCurrentOrderRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                repayApplicationService.setCurrentOrder(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
