package com.pk.app.push.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.push.application.PushPopupApplicationService;
import com.pk.app.push.dto.response.PendingPopupListResponse;
import com.pk.app.push.dto.response.PopupDisplayedResponse;
import com.pk.app.security.SecurityContextSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Mandatory in-app popup pull fallback and display reporting. */
@RestController
@RequestMapping("/push/popups")
public class PushPopupController {
    private final PushPopupApplicationService pushPopupApplicationService;

    public PushPopupController(PushPopupApplicationService pushPopupApplicationService) {
        this.pushPopupApplicationService = pushPopupApplicationService;
    }

    @GetMapping("/pending")
    public ApiResponse<PendingPopupListResponse> pending(HttpServletRequest httpRequest) {
        return ApiResponse.success(
                new PendingPopupListResponse(
                        pushPopupApplicationService.pendingPopups(SecurityContextSupport.requirePrincipal())
                ),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/{popupId}/displayed")
    public ApiResponse<PopupDisplayedResponse> displayed(
            @PathVariable long popupId,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                pushPopupApplicationService.markDisplayed(SecurityContextSupport.requirePrincipal(), popupId),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
