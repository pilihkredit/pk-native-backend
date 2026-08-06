package com.pk.app.debug.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.debug.application.DebugPushSendApplicationService;
import com.pk.app.debug.dto.DebugPushSendRequest;
import com.pk.app.debug.dto.DebugPushSendResponse;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug/push")
@PublicApi
@CrossOrigin(
        originPatterns = {"https://*.aiforce.cloud", "https://*.feishu.cn", "http://localhost:*"},
        allowedHeaders = {"X-Debug-Token", "Content-Type"},
        exposedHeaders = {"Content-Type"}
)
public class DebugPushSendController {
    private final DebugPushSendApplicationService applicationService;

    public DebugPushSendController(DebugPushSendApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/send")
    public ApiResponse<DebugPushSendResponse> send(
            @RequestHeader(value = "X-Debug-Token", required = false) String debugToken,
            @Valid @RequestBody DebugPushSendRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                applicationService.send(debugToken, request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
