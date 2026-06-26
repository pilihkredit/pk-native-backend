package com.pk.app.loan.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.loan.application.LoanApplyApplicationService;
import com.pk.app.repay.application.LoanBillsApplicationService;
import com.pk.app.loan.application.LoanProductApplicationService;
import com.pk.app.loan.application.LoanTrialApplicationService;
import com.pk.app.loan.dto.request.LoanApplyRequest;
import com.pk.app.loan.dto.request.LoanTrialRequest;
import com.pk.app.loan.dto.response.LoanApplyResponse;
import com.pk.app.loan.dto.response.LoanBillsResponse;
import com.pk.app.loan.dto.response.LoanProductsResponse;
import com.pk.app.loan.dto.response.LoanStatusResponse;
import com.pk.app.loan.dto.response.LoanTrialResponse;
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
@RequestMapping("/loan")
public class LoanController {
    private final LoanProductApplicationService loanProductApplicationService;
    private final LoanTrialApplicationService loanTrialApplicationService;
    private final LoanApplyApplicationService loanApplyApplicationService;
    private final LoanBillsApplicationService loanBillsApplicationService;

    public LoanController(
            LoanProductApplicationService loanProductApplicationService,
            LoanTrialApplicationService loanTrialApplicationService,
            LoanApplyApplicationService loanApplyApplicationService,
            LoanBillsApplicationService loanBillsApplicationService
    ) {
        this.loanProductApplicationService = loanProductApplicationService;
        this.loanTrialApplicationService = loanTrialApplicationService;
        this.loanApplyApplicationService = loanApplyApplicationService;
        this.loanBillsApplicationService = loanBillsApplicationService;
    }

    @GetMapping("/products")
    public ApiResponse<LoanProductsResponse> listProducts(
            @RequestParam("applyId") String applyId,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                loanProductApplicationService.listProducts(principal, applyId),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/trial")
    public ApiResponse<LoanTrialResponse> trial(
            @Valid @RequestBody LoanTrialRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                loanTrialApplicationService.trial(principal, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/apply")
    public ApiResponse<LoanApplyResponse> apply(
            @Valid @RequestBody LoanApplyRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                loanApplyApplicationService.apply(principal, request, httpRequest),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/bills")
    public ApiResponse<LoanBillsResponse> listBills(
            @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                loanBillsApplicationService.listBills(principal, status),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/{loanApplyId}/status")
    public ApiResponse<LoanStatusResponse> getStatus(
            @PathVariable("loanApplyId") String loanApplyId,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                loanApplyApplicationService.getStatus(principal, loanApplyId),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
