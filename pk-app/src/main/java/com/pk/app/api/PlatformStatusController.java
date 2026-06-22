package com.pk.app.api;

import com.pk.app.web.RequestSupport;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pk/v1")
public class PlatformStatusController {
    @GetMapping("/platform/status")
    public ApiResponse<PlatformStatus> status(HttpServletRequest request) {
        String traceId = RequestSupport.traceId(request);
        return ApiResponse.success(new PlatformStatus("UP", Instant.now()), traceId);
    }
}
