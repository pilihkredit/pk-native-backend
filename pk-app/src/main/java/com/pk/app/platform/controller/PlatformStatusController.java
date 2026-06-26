package com.pk.app.platform.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.platform.dto.PlatformStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform
 *
 * Platform health probes.
 */
@RestController
@RequestMapping("/platform")
public class PlatformStatusController {

    /** Platform status. */
    @GetMapping("/status")
    public ApiResponse<PlatformStatus> status(HttpServletRequest request) {
        return ApiResponse.success(
                new PlatformStatus("UP", Instant.now()),
                RequestTrace.resolveTraceId(request)
        );
    }
}
