package com.pk.app.tracking.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.security.SecurityContextSupport;
import com.pk.app.tracking.application.TrackingApplicationService;
import com.pk.app.tracking.dto.request.TrackingEventsRequest;
import com.pk.app.tracking.dto.response.TrackingEventsResponse;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PublicApi
@RestController
@RequestMapping("/tracking")
public class TrackingController {
    private final TrackingApplicationService trackingApplicationService;

    public TrackingController(TrackingApplicationService trackingApplicationService) {
        this.trackingApplicationService = trackingApplicationService;
    }

    @PostMapping("/events")
    public ApiResponse<TrackingEventsResponse> ingestEvents(
            @Valid @RequestBody TrackingEventsRequest request,
            @RequestHeader(value = "X-Device-No", required = false) String deviceNo,
            HttpServletRequest httpRequest
    ) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        return ApiResponse.success(
                trackingApplicationService.ingest(principal, deviceNo, resolveClientIp(httpRequest), request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int commaIndex = forwardedFor.indexOf(',');
            return commaIndex < 0 ? forwardedFor.trim() : forwardedFor.substring(0, commaIndex).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
