package com.pk.app.bank.controller;

import com.pk.app.bank.application.BankApplicationService;
import com.pk.app.bank.dto.response.BankListItemResponse;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bank reference data for onboarding.
 */
@RestController
@RequestMapping("/bank")
public class BankController {
    private final BankApplicationService bankApplicationService;

    public BankController(BankApplicationService bankApplicationService) {
        this.bankApplicationService = bankApplicationService;
    }

    /** List banks. */
    @GetMapping("/list")
    public ApiResponse<List<BankListItemResponse>> listBanks(HttpServletRequest httpRequest) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                bankApplicationService.listBanks(),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
