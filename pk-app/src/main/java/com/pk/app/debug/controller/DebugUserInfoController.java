package com.pk.app.debug.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.debug.application.DebugUserInfoApplicationService;
import com.pk.app.debug.dto.DebugUserInfoResponse;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug/user-info")
@PublicApi
@CrossOrigin(
        originPatterns = {"https://*.aiforce.cloud", "https://*.feishu.cn", "http://localhost:*"},
        allowedHeaders = {"X-Debug-Token", "Content-Type"},
        exposedHeaders = {"Content-Type"}
)
public class DebugUserInfoController {
    private final DebugUserInfoApplicationService applicationService;

    public DebugUserInfoController(DebugUserInfoApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ApiResponse<DebugUserInfoResponse> query(
            @RequestHeader(value = "X-Debug-Token", required = false) String debugToken,
            @RequestParam("mobileNo") String mobileNo,
            HttpServletRequest request
    ) {
        return ApiResponse.success(
                applicationService.query(debugToken, mobileNo),
                RequestTrace.resolveTraceId(request)
        );
    }
}
