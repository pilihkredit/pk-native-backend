package com.pk.app.debug.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.debug.application.DebugTrackingEventsApplicationService;
import com.pk.app.debug.dto.DebugTrackingEventsResponse;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug/tracking-events")
@PublicApi
@CrossOrigin(
        originPatterns = {"https://*.aiforce.cloud", "https://*.feishu.cn", "http://localhost:*"},
        allowedHeaders = {"X-Debug-Token", "Content-Type"},
        exposedHeaders = {"Content-Type"}
)
public class DebugTrackingEventsController {
    private final DebugTrackingEventsApplicationService applicationService;

    public DebugTrackingEventsController(DebugTrackingEventsApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ApiResponse<DebugTrackingEventsResponse> query(
            @RequestHeader(value = "X-Debug-Token", required = false) String debugToken,
            @RequestParam(value = "clientNo", required = false) String clientNo,
            @RequestParam(value = "mobileNo", required = false) String mobileNo,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request
    ) {
        return ApiResponse.success(
                applicationService.query(debugToken, clientNo, mobileNo, userId, limit),
                RequestTrace.resolveTraceId(request)
        );
    }
}
