package com.pk.app.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pk/v1")
public class PlatformStatusController {
    @GetMapping("/platform/status")
    public ApiResponse<PlatformStatus> status(HttpServletRequest request) {
        String traceId = resolveTraceId(request);
        return ApiResponse.success(new PlatformStatus("UP", Instant.now()), traceId);
    }

    private String resolveTraceId(HttpServletRequest request) {
        String header = request.getHeader("X-Trace-Id");
        if (header != null && !header.isBlank()) {
            return header;
        }
        return UUID.randomUUID().toString();
    }
}
