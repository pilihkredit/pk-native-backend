package com.pk.app.launch.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.launch.application.AppLaunchApplicationService;
import com.pk.app.launch.dto.request.AppLaunchRecordRequest;
import com.pk.app.launch.dto.response.AppLaunchRecordResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PublicApi
@RestController
@RequestMapping("/app/launches")
public class AppLaunchController {
    private final AppLaunchApplicationService appLaunchApplicationService;

    public AppLaunchController(AppLaunchApplicationService appLaunchApplicationService) {
        this.appLaunchApplicationService = appLaunchApplicationService;
    }

    @PostMapping
    public ApiResponse<AppLaunchRecordResponse> record(
            @Valid @RequestBody AppLaunchRecordRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                appLaunchApplicationService.record(
                        SecurityContextSupport.requirePrincipal(),
                        request,
                        ClientRequestHeaders.require(httpRequest)
                ),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
