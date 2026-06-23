package com.pk.app.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pk/v1/platform")
public class PlatformStatusController {
    @GetMapping("/status")
    public ApiResponse<PlatformStatus> status(HttpServletRequest request) {
        return ApiResponse.success(
                new PlatformStatus("UP", Instant.now()),
                RequestTrace.resolveTraceId(request)
        );
    }
}
