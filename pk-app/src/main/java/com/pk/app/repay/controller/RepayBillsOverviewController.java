package com.pk.app.repay.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.repay.application.RepayBillsOverviewApplicationService;
import com.pk.app.repay.dto.response.RepayBillsOverviewResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repay")
public class RepayBillsOverviewController {
    private final RepayBillsOverviewApplicationService repayBillsOverviewApplicationService;

    public RepayBillsOverviewController(RepayBillsOverviewApplicationService repayBillsOverviewApplicationService) {
        this.repayBillsOverviewApplicationService = repayBillsOverviewApplicationService;
    }

    @GetMapping("/bills-overview")
    public ApiResponse<RepayBillsOverviewResponse> billsOverview(HttpServletRequest httpRequest) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                repayBillsOverviewApplicationService.billsOverview(principal),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
