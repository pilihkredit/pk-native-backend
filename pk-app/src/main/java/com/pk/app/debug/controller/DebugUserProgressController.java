package com.pk.app.debug.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.debug.application.DebugUserProgressApplicationService;
import com.pk.app.debug.dto.DebugUserProgressResponse;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug/user-progress")
@PublicApi
@CrossOrigin(
        originPatterns = {"https://*.aiforce.cloud", "https://*.feishu.cn", "http://localhost:*"},
        allowedHeaders = {"Content-Type"},
        exposedHeaders = {"Content-Type"}
)
public class DebugUserProgressController {
    private final DebugUserProgressApplicationService applicationService;

    public DebugUserProgressController(DebugUserProgressApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ApiResponse<DebugUserProgressResponse> query(
            @RequestParam("mobileNo") String mobileNo,
            HttpServletRequest request
    ) {
        return ApiResponse.success(
                applicationService.query(mobileNo),
                RequestTrace.resolveTraceId(request)
        );
    }
}
