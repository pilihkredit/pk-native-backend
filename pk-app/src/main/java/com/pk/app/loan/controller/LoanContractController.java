package com.pk.app.loan.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.loan.application.LoanContractApplicationService;
import com.pk.app.loan.dto.response.LoanContractsResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loan")
public class LoanContractController {
    private final LoanContractApplicationService loanContractApplicationService;

    public LoanContractController(LoanContractApplicationService loanContractApplicationService) {
        this.loanContractApplicationService = loanContractApplicationService;
    }

    @GetMapping("/{loanApplyId}/contracts")
    public ApiResponse<LoanContractsResponse> listContracts(
            @PathVariable("loanApplyId") String loanApplyId,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                loanContractApplicationService.listContracts(principal, loanApplyId),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
