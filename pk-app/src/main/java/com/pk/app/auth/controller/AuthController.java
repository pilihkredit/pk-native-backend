package com.pk.app.auth.controller;

import com.pk.app.auth.application.AuthApplicationService;
import com.pk.app.auth.dto.request.MobileCheckRequest;
import com.pk.app.auth.dto.request.PasswordLoginRequest;
import com.pk.app.auth.dto.request.PasswordChangeRequest;
import com.pk.app.auth.dto.request.PasswordSetRequest;
import com.pk.app.auth.dto.request.OtpSendRequest;
import com.pk.app.auth.dto.request.OtpVerifyRequest;
import com.pk.app.auth.dto.request.RefreshTokenRequest;
import com.pk.app.auth.dto.request.WhatsAppLoginRequest;
import com.pk.app.auth.dto.response.AccountCloseEligibilityResponse;
import com.pk.app.auth.dto.response.PasswordChangeResponse;
import com.pk.app.auth.dto.response.MobileCheckResponse;
import com.pk.app.auth.dto.response.PasswordSetResponse;
import com.pk.app.auth.dto.response.OtpSendResponse;
import com.pk.app.auth.dto.response.OtpVerifyResponse;
import com.pk.app.auth.dto.response.RefreshTokenResponse;
import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.security.SecurityContextSupport;
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
 * Auth
 *
 * OTP login, token refresh, and logout.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    /** Check mobile registration. */
    @PublicApi
    @PostMapping("/mobile/check")
    public ApiResponse<MobileCheckResponse> checkMobile(
            @Valid @RequestBody MobileCheckRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String ignoredAuthorization,
            @RequestHeader(value = "X-Device-No", required = false) String deviceNoHeader,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.checkMobile(request, deviceNoHeader),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Send OTP. */
    @PublicApi
    @PostMapping("/otp/send")
    public ApiResponse<OtpSendResponse> sendOtp(
            @Valid @RequestBody OtpSendRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String ignoredAuthorization,
            @RequestHeader(value = "X-Device-No", required = false) String deviceNoHeader,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.sendOtp(request, deviceNoHeader),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Verify OTP. */
    @PublicApi
    @PostMapping("/otp/verify")
    public ApiResponse<OtpVerifyResponse> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            @RequestHeader(value = "X-Device-No", required = false) String deviceNoHeader,
            @RequestHeader(value = "X-Platform", required = false) String platformHeader,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.verifyOtp(request, deviceNoHeader, platformHeader),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Login with WhatsApp OTP. No otpToken; challenge is resolved by mobileNo. */
    @PublicApi
    @PostMapping("/login-whatsapp")
    public ApiResponse<OtpVerifyResponse> loginWhatsApp(
            @Valid @RequestBody WhatsAppLoginRequest request,
            @RequestHeader(value = "X-Device-No", required = false) String deviceNoHeader,
            @RequestHeader(value = "X-Platform", required = false) String platformHeader,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.loginWithWhatsApp(request, deviceNoHeader, platformHeader),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Refresh access token. */
    @PublicApi
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.refresh(request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Logout. */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        authApplicationService.logout(
                SecurityContextSupport.requirePrincipal(),
                httpRequest.getHeader("X-Device-No")
        );
        return ApiResponse.success(null, RequestTrace.resolveTraceId(httpRequest));
    }

    /** Check whether the current account is eligible for closure. */
    @PostMapping("/close-account/check")
    public ApiResponse<AccountCloseEligibilityResponse> checkAccountCloseEligibility(
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.checkAccountCloseEligibility(
                        SecurityContextSupport.requirePrincipal(),
                        httpRequest
                ),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Set login password for the first time. */
    @PostMapping("/password/set")
    public ApiResponse<PasswordSetResponse> setPassword(
            @Valid @RequestBody PasswordSetRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.setPassword(SecurityContextSupport.requirePrincipal(), request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Change an existing login password. Active sessions are kept. */
    @PostMapping("/password/change")
    public ApiResponse<PasswordChangeResponse> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.changePassword(SecurityContextSupport.requirePrincipal(), request),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /** Login with mobile number and password. */
    @PublicApi
    @PostMapping("/password/login")
    public ApiResponse<OtpVerifyResponse> loginByPassword(
            @Valid @RequestBody PasswordLoginRequest request,
            @RequestHeader(value = "X-Device-No", required = false) String deviceNoHeader,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.loginByPassword(request, deviceNoHeader),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
