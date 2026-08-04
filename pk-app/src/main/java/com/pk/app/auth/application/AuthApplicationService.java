package com.pk.app.auth.application;

import com.pk.app.auth.dto.request.PasswordLoginRequest;
import com.pk.app.auth.dto.request.PasswordSetRequest;
import com.pk.app.auth.dto.request.OtpSendRequest;
import com.pk.app.auth.dto.request.OtpVerifyRequest;
import com.pk.app.auth.dto.request.WhatsAppLoginRequest;
import com.pk.app.auth.dto.request.RefreshTokenRequest;
import com.pk.app.auth.dto.request.MobileCheckRequest;
import com.pk.app.auth.dto.response.AccountCloseEligibilityResponse;
import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.profile.application.ProfileDeviceResolver;
import com.pk.app.auth.dto.response.MobileCheckResponse;
import com.pk.app.auth.dto.response.OtpSendResponse;
import com.pk.app.auth.dto.response.OtpVerifyResponse;
import com.pk.app.auth.dto.response.PasswordSetResponse;
import com.pk.app.auth.dto.response.RefreshTokenResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.TokenPair;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.home.HomeUserStage;
import com.pk.app.home.application.HomeApplicationService;
import com.pk.infra.auth.AuthServiceFacade;
import com.pk.infra.auth.AccountCloseAccessFacade;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {
    private static final Logger log = LoggerFactory.getLogger(AuthApplicationService.class);
    private final AuthServiceFacade authServiceFacade;
    private final HomeApplicationService homeApplicationService;
    private final AccountCloseAccessFacade accountCloseAccessFacade;
    private final ProfileDeviceResolver profileDeviceResolver;

    public AuthApplicationService(
            AuthServiceFacade authServiceFacade,
            HomeApplicationService homeApplicationService,
            AccountCloseAccessFacade accountCloseAccessFacade,
            ProfileDeviceResolver profileDeviceResolver
    ) {
        this.authServiceFacade = authServiceFacade;
        this.homeApplicationService = homeApplicationService;
        this.accountCloseAccessFacade = accountCloseAccessFacade;
        this.profileDeviceResolver = profileDeviceResolver;
    }

    public MobileCheckResponse checkMobile(MobileCheckRequest request, String deviceNoHeader) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.MobileCheckResult result = authServiceFacade.checkMobileRegistration(
                request.mobileNo(),
                request.deviceNo()
        );
        return new MobileCheckResponse(result.registered(), result.accountStatus(), result.passwordSet());
    }

    public OtpSendResponse sendOtp(OtpSendRequest request, String deviceNoHeader) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.OtpSendResult result = authServiceFacade.sendOtp(request.mobileNo(), request.deviceNo());
        return new OtpSendResponse(result.otpToken(), result.expireIn(), result.resendAfter());
    }

    public OtpSendResponse sendWhatsAppCode(OtpSendRequest request, String deviceNoHeader) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.OtpSendResult result = authServiceFacade.sendWhatsAppCode(
                request.mobileNo(),
                request.deviceNo()
        );
        return new OtpSendResponse(result.otpToken(), result.expireIn(), result.resendAfter());
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request, String deviceNoHeader) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.OtpVerifyResult result = authServiceFacade.verifyOtp(
                request.mobileNo(),
                request.otpToken(),
                request.otpCode(),
                request.deviceNo()
        );
        return toOtpSessionResponse(result);
    }

    public OtpVerifyResponse verifyOtp(
            OtpVerifyRequest request,
            String deviceNoHeader,
            String platformHeader
    ) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.OtpVerifyResult result = authServiceFacade.verifyOtp(
                request.mobileNo(),
                request.otpToken(),
                request.otpCode(),
                request.deviceNo(),
                platformHeader
        );
        return toOtpSessionResponse(result);
    }

    public OtpVerifyResponse loginWithWhatsApp(WhatsAppLoginRequest request, String deviceNoHeader) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.OtpVerifyResult result = authServiceFacade.loginWithWhatsApp(
                request.mobileNo(),
                request.otpCode(),
                request.deviceNo()
        );
        return toOtpSessionResponse(result);
    }

    public OtpVerifyResponse loginWithWhatsApp(
            WhatsAppLoginRequest request,
            String deviceNoHeader,
            String platformHeader
    ) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.OtpVerifyResult result = authServiceFacade.loginWithWhatsApp(
                request.mobileNo(),
                request.otpCode(),
                request.deviceNo(),
                platformHeader
        );
        return toOtpSessionResponse(result);
    }

    private OtpVerifyResponse toOtpSessionResponse(AuthServiceFacade.OtpVerifyResult result) {
        UserProfileSummary profile = result.profile();
        TokenPair tokenPair = result.tokenPair();
        return new OtpVerifyResponse(
                profile.partnerUserId(),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.tokenType(),
                tokenPair.accessTokenExpiresInSeconds(),
                profile.newlyCreated() ? "REGISTER" : "LOGIN",
                profile.newlyCreated(),
                result.passwordSet(),
                resolveUserStageForLogin(profile.userId(), profile.partnerUserId())
        );
    }

    /**
     * Login session responses must not fail when lender has no user yet (A000010 / L000010),
     * e.g. modules incomplete / not yet synced. Treat as ONBOARDING so the client can continue.
     * Other callers of onboarding progress still surface L000010 unchanged.
     */
    private String resolveUserStageForLogin(long userId, String partnerUserId) {
        try {
            return homeApplicationService.resolveUserStage(userId, partnerUserId);
        } catch (ApiException exception) {
            if (exception.apiCode() == ApiCode.UPSTREAM_APPLICATION_NOT_FOUND) {
                return HomeUserStage.ONBOARDING;
            }
            throw exception;
        }
    }

    public PasswordSetResponse setPassword(AuthenticatedPrincipal principal, PasswordSetRequest request) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        authServiceFacade.setPassword(principal.userId(), request.password(), request.confirmPassword());
        return new PasswordSetResponse(true);
    }

    public OtpVerifyResponse loginByPassword(PasswordLoginRequest request, String deviceNoHeader) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.PasswordLoginResult result = authServiceFacade.loginByPassword(
                request.mobileNo(),
                request.password(),
                request.deviceNo()
        );
        return toSessionResponse(result.profile(), result.tokenPair(), result.passwordSet());
    }

    private OtpVerifyResponse toSessionResponse(
            UserProfileSummary profile,
            TokenPair tokenPair,
            boolean passwordSet
    ) {
        return new OtpVerifyResponse(
                profile.partnerUserId(),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.tokenType(),
                tokenPair.accessTokenExpiresInSeconds(),
                "LOGIN",
                false,
                passwordSet,
                resolveUserStageForLogin(profile.userId(), profile.partnerUserId())
        );
    }

    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        TokenPair tokenPair = authServiceFacade.refresh(request.refreshToken());
        return new RefreshTokenResponse(
                tokenPair.accessToken(),
                tokenPair.tokenType(),
                tokenPair.accessTokenExpiresInSeconds()
        );
    }

    public void logout(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        authServiceFacade.logout(principal);
    }

    public AccountCloseEligibilityResponse checkAccountCloseEligibility(
            AuthenticatedPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        return new AccountCloseEligibilityResponse(checkAccountCloseAccess(principal, httpRequest));
    }

    private boolean checkAccountCloseAccess(AuthenticatedPrincipal principal, HttpServletRequest httpRequest) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        var device = profileDeviceResolver.resolve(principal, ClientRequestHeaders.require(httpRequest));
        return accountCloseAccessFacade.checkAccess(principal, device).canClose();
    }

    private static void validateDeviceNoMatchesHeader(String bodyDeviceNo, String headerDeviceNo) {
        if (headerDeviceNo != null && !headerDeviceNo.isBlank() && !headerDeviceNo.equals(bodyDeviceNo)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }
}
