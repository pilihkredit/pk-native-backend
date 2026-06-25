package com.pk.app.home.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.home.application.HomeApplicationService;
import com.pk.app.home.dto.response.HomeSummaryResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Post-login home routing.
 */
@RestController
@RequestMapping("/home")
public class HomeController {
    private final HomeApplicationService homeApplicationService;

    public HomeController(HomeApplicationService homeApplicationService) {
        this.homeApplicationService = homeApplicationService;
    }

    /** Query user stage for home routing. */
    @GetMapping("/summary")
    public ApiResponse<HomeSummaryResponse> getSummary(HttpServletRequest httpRequest) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                homeApplicationService.getSummary(principal),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
