package com.pk.app.api;

import com.pk.app.auth.AuthApplicationService;
import com.pk.app.auth.DisclosureService;
import com.pk.app.web.RequestSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pk/v1")
public class AuthController {
    private final AuthApplicationService authApplicationService;
    private final DisclosureService disclosureService;

    public AuthController(AuthApplicationService authApplicationService, DisclosureService disclosureService) {
        this.authApplicationService = authApplicationService;
        this.disclosureService = disclosureService;
    }

    @GetMapping("/app/disclosure/permission")
    public ApiResponse<DisclosureService.DisclosureConfig> permissionDisclosure(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "APP_LAUNCH") String scene
    ) {
        String locale = request.getHeader("Accept-Language");
        var data = disclosureService.getPermissionDisclosure(scene, locale);
        return ApiResponse.success(data, RequestSupport.traceId(request));
    }

    @PostMapping("/auth/mobile/check")
    public ApiResponse<AuthApplicationService.MobileCheckResult> checkMobile(
            HttpServletRequest request,
            @Valid @RequestBody com.pk.app.api.auth.MobileCheckRequest body
    ) {
        RequestSupport.assertDeviceMatches(request, body.deviceNo());
        var data = authApplicationService.checkMobile(body.mobileNo(), body.deviceNo());
        return ApiResponse.success(data, RequestSupport.traceId(request));
    }

    @PostMapping("/auth/otp/send")
    public ApiResponse<OtpSendResponse> sendOtp(
            HttpServletRequest request,
            @Valid @RequestBody com.pk.app.api.auth.OtpSendRequest body
    ) {
        RequestSupport.assertDeviceMatches(request, body.deviceNo());
        var result = authApplicationService.sendOtp(body.mobileNo(), body.deviceNo());
        return ApiResponse.success(
                new OtpSendResponse(result.otpToken(), result.expireIn(), result.resendAfter()),
                RequestSupport.traceId(request)
        );
    }

    @PostMapping("/auth/otp/verify")
    public ApiResponse<AuthApplicationService.VerifyOtpResult> verifyOtp(
            HttpServletRequest request,
            @Valid @RequestBody com.pk.app.api.auth.OtpVerifyRequest body
    ) {
        RequestSupport.assertDeviceMatches(request, body.deviceNo());
        var data = authApplicationService.verifyOtp(
                body.mobileNo(),
                body.deviceNo(),
                body.otpToken(),
                body.otpCode()
        );
        return ApiResponse.success(data, RequestSupport.traceId(request));
    }

    public record OtpSendResponse(String otpToken, int expireIn, int resendAfter) {
    }
}
