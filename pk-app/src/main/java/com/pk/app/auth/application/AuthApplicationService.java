package com.pk.app.auth.application;

import com.pk.app.auth.dto.request.PasswordLoginRequest;
import com.pk.app.auth.dto.request.PasswordSetRequest;
import com.pk.app.auth.dto.request.OtpSendRequest;
import com.pk.app.auth.dto.request.OtpVerifyRequest;
import com.pk.app.auth.dto.request.RefreshTokenRequest;
import com.pk.app.auth.dto.request.MobileCheckRequest;
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
import com.pk.app.home.application.HomeApplicationService;
import com.pk.infra.auth.AuthServiceFacade;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {
    private final AuthServiceFacade authServiceFacade;
    private final HomeApplicationService homeApplicationService;

    public AuthApplicationService(
            AuthServiceFacade authServiceFacade,
            HomeApplicationService homeApplicationService
    ) {
        this.authServiceFacade = authServiceFacade;
        this.homeApplicationService = homeApplicationService;
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

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request, String deviceNoHeader) {
        validateDeviceNoMatchesHeader(request.deviceNo(), deviceNoHeader);
        AuthServiceFacade.OtpVerifyResult result = authServiceFacade.verifyOtp(
                request.mobileNo(),
                request.otpToken(),
                request.otpCode(),
                request.deviceNo()
        );
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
                homeApplicationService.resolveUserStage(profile.profileId(), profile.partnerUserId())
        );
    }

    public PasswordSetResponse setPassword(AuthenticatedPrincipal principal, PasswordSetRequest request) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        authServiceFacade.setPassword(principal.profileId(), request.password(), request.confirmPassword());
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
                homeApplicationService.resolveUserStage(profile.profileId(), profile.partnerUserId())
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

    private static void validateDeviceNoMatchesHeader(String bodyDeviceNo, String headerDeviceNo) {
        if (headerDeviceNo != null && !headerDeviceNo.isBlank() && !headerDeviceNo.equals(bodyDeviceNo)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }
}
