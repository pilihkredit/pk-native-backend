package com.pk.app.push.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.push.application.PushApplicationService;
import com.pk.app.push.dto.response.InboxMessagePageResponse;
import com.pk.app.push.dto.response.InboxMessageReadResponse;
import com.pk.app.push.dto.response.InboxMessageResponse;
import com.pk.app.push.dto.response.UnreadMessageCountResponse;
import com.pk.app.security.SecurityContextSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/messages")
public class InboxMessageController {
    private final PushApplicationService pushApplicationService;

    public InboxMessageController(PushApplicationService pushApplicationService) {
        this.pushApplicationService = pushApplicationService;
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadMessageCountResponse> unreadCount(HttpServletRequest httpRequest) {
        return ApiResponse.success(
                pushApplicationService.unreadCount(SecurityContextSupport.requirePrincipal()),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping
    public ApiResponse<InboxMessagePageResponse> list(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int pageSize,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                pushApplicationService.list(SecurityContextSupport.requirePrincipal(), cursor, pageSize),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @GetMapping("/{messageId}")
    public ApiResponse<InboxMessageResponse> detail(
            @PathVariable long messageId,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                pushApplicationService.detail(SecurityContextSupport.requirePrincipal(), messageId),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    @PostMapping("/{messageId}/read")
    public ApiResponse<InboxMessageReadResponse> markRead(
            @PathVariable long messageId,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                pushApplicationService.markRead(SecurityContextSupport.requirePrincipal(), messageId),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
