package com.pk.app.auth.controller;

import com.pk.app.auth.application.AuthApplicationService;
import com.pk.app.auth.dto.request.MobileCheckRequest;
import com.pk.app.auth.dto.request.OtpSendRequest;
import com.pk.app.auth.dto.request.OtpVerifyRequest;
import com.pk.app.auth.dto.request.RefreshTokenRequest;
import com.pk.app.auth.dto.response.MobileCheckResponse;
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

    /**
     * Check mobile registration status
     *
     * Called before OTP send to route UI for returning vs new users. Public endpoint.
     * Does not issue tokens; authoritative register/login remains {@link #verifyOtp}.
     *
     * @param request              mobile number and device id
     * @param ignoredAuthorization optional Authorization header, ignored
     * @param deviceNoHeader       optional X-Device-No header; when present must equal request.deviceNo
     * @param httpRequest          servlet request for trace id
     * @return registered flag and accountStatus (EXISTING or NEW)
     */
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

    /**
     * Send OTP
     *
     * Sends OTP to a mobile number. Public endpoint; Authorization header is ignored.
     * Same device cannot resend within resendAfter seconds (default 60s).
     *
     * @param request              mobile number and device id
     * @param ignoredAuthorization optional Authorization header, ignored
     * @param deviceNoHeader       optional X-Device-No header; when present must equal request.deviceNo
     * @param httpRequest          servlet request for trace id
     * @return otpToken, expireIn, resendAfter
     */
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

    /**
     * Verify OTP
     *
     * Verifies OTP and opens a session. Public endpoint.
     * New mobiles are auto-registered. Login on a new device invalidates previous sessions.
     *
     * @param request        OTP verify payload
     * @param deviceNoHeader optional X-Device-No header; when present must equal request.deviceNo
     * @param httpRequest    servlet request for trace id
     * @return tokens and user summary
     */
    @PublicApi
    @PostMapping("/otp/verify")
    public ApiResponse<OtpVerifyResponse> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            @RequestHeader(value = "X-Device-No", required = false) String deviceNoHeader,
            HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(
                authApplicationService.verifyOtp(request, deviceNoHeader),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }

    /**
     * Refresh Token
     *
     * Exchanges refresh token for a new access token. Public endpoint.
     * Call on HTTP 401 or before access token expiry. Refresh token is not rotated.
     *
     * @param request     refresh token body
     * @param httpRequest servlet request for trace id
     * @return new access token and expiresIn
     */
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

    /**
     * Logout
     *
     * Invalidates current session and all refresh tokens. Requires Bearer access token.
     *
     * @param httpRequest servlet request for trace id
     * @return empty success payload
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        authApplicationService.logout(SecurityContextSupport.requirePrincipal());
        return ApiResponse.success(null, RequestTrace.resolveTraceId(httpRequest));
    }
}
