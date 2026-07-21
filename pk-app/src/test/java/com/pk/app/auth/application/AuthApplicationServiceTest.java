package com.pk.app.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.app.auth.dto.request.MobileCheckRequest;
import com.pk.app.auth.dto.request.OtpVerifyRequest;
import com.pk.app.home.application.HomeApplicationService;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.TokenPair;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.home.HomeUserStage;
import com.pk.infra.auth.AuthServiceFacade;
import org.junit.jupiter.api.Test;

class AuthApplicationServiceTest {
    @Test
    void mapsMobileCheckResultToResponse() {
        AuthServiceFacade facade = mock(AuthServiceFacade.class);
        HomeApplicationService homeApplicationService = mock(HomeApplicationService.class);
        when(facade.checkMobileRegistration("8123456789", "device-1"))
                .thenReturn(new AuthServiceFacade.MobileCheckResult(true, "EXISTING", true));

        MobileCheckRequest request = new MobileCheckRequest("8123456789", "device-1");
        var response = new AuthApplicationService(facade, homeApplicationService).checkMobile(request, "device-1");

        assertThat(response.registered()).isTrue();
        assertThat(response.accountStatus()).isEqualTo("EXISTING");
        assertThat(response.passwordSet()).isTrue();
        verify(facade).checkMobileRegistration("8123456789", "device-1");
    }

    @Test
    void otpVerifyReturnsOnboardingWhenLenderUserNotFound() {
        AuthServiceFacade facade = mock(AuthServiceFacade.class);
        HomeApplicationService homeApplicationService = mock(HomeApplicationService.class);
        UserProfileSummary profile = new UserProfileSummary(10L, "U10001", "81234567890", true);
        TokenPair tokenPair = new TokenPair("access", "refresh", 900, "Bearer");
        when(facade.verifyOtp("81234567890", "otp-token", "123456", "device-1"))
                .thenReturn(new AuthServiceFacade.OtpVerifyResult(profile, tokenPair, false));
        when(homeApplicationService.resolveUserStage(10L, "U10001"))
                .thenThrow(new ApiException(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND, "data tidak ada"));

        var response = new AuthApplicationService(facade, homeApplicationService).verifyOtp(
                new OtpVerifyRequest("81234567890", "otp-token", "123456", "device-1"),
                "device-1"
        );

        assertThat(response.userStage()).isEqualTo(HomeUserStage.ONBOARDING);
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.newUser()).isTrue();
    }

    @Test
    void otpVerifyPropagatesOtherStageErrors() {
        AuthServiceFacade facade = mock(AuthServiceFacade.class);
        HomeApplicationService homeApplicationService = mock(HomeApplicationService.class);
        UserProfileSummary profile = new UserProfileSummary(10L, "U10001", "81234567890", false);
        TokenPair tokenPair = new TokenPair("access", "refresh", 900, "Bearer");
        when(facade.verifyOtp("81234567890", "otp-token", "123456", "device-1"))
                .thenReturn(new AuthServiceFacade.OtpVerifyResult(profile, tokenPair, true));
        when(homeApplicationService.resolveUserStage(10L, "U10001"))
                .thenThrow(new ApiException(ApiCode.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> new AuthApplicationService(facade, homeApplicationService).verifyOtp(
                new OtpVerifyRequest("81234567890", "otp-token", "123456", "device-1"),
                "device-1"
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);
    }
}
