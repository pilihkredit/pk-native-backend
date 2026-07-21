package com.pk.app.auth.controller;

import com.pk.app.auth.application.AuthApplicationService;
import com.pk.app.auth.dto.request.OtpSendRequest;
import com.pk.app.auth.dto.response.OtpSendResponse;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.core.auth.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WhatsApp OTP send (login via {@code POST /auth/login-whatsapp}).
 */
@RestController
@RequestMapping("/whatsapp")
public class WhatsAppController {
    private final AuthApplicationService authApplicationService;

    public WhatsAppController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    /** Send WhatsApp OTP. Headers and body align with {@code POST /auth/otp/send}. */
    @PublicApi
    @PostMapping("/send-code")
    public ApiResponse<OtpSendResponse> sendCode(
            @Valid @RequestBody OtpSendRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String ignoredAuthorization,
            @RequestHeader(value = "X-Device-No", required = false) String deviceNoHeader,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.sendWhatsAppCode(request, deviceNoHeader),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
