package com.pk.app.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.error.AppBusinessException;
import com.pk.core.error.AppErrorCodes;
import com.pk.infra.user.UserProfileRecord;
import com.pk.infra.user.UserProfileRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private OtpService otpService;
    @Mock
    private JwtTokenService jwtTokenService;

    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        authApplicationService = new AuthApplicationService(userProfileRepository, otpService, jwtTokenService);
    }

    @Test
    void checkMobileReturnsNewWhenNotRegistered() {
        when(userProfileRepository.findActiveByMobileNo("81234567890")).thenReturn(Optional.empty());

        var result = authApplicationService.checkMobile("81234567890", "device-1");

        assertThat(result.registered()).isFalse();
        assertThat(result.accountStatus()).isEqualTo("NEW");
        verify(otpService).enforceSendRateLimit("81234567890", "device-1");
    }

    @Test
    void verifyOtpRegistersNewUser() {
        when(userProfileRepository.findActiveByMobileNo("81234567890")).thenReturn(Optional.empty());
        var profile = new UserProfileRecord(1L, "U00001", "81234567890", "INCOMPLETE", Instant.now());
        when(userProfileRepository.insertNewUser("81234567890")).thenReturn(profile);
        when(jwtTokenService.issueToken(1L, "U00001", "81234567890"))
                .thenReturn(new JwtTokenService.IssuedToken("token-1", 604800L));

        var result = authApplicationService.verifyOtp("81234567890", "device-1", "OTP_1", "123456");

        assertThat(result.newUser()).isTrue();
        assertThat(result.authAction()).isEqualTo("REGISTER");
        assertThat(result.userStage()).isEqualTo("ONBOARDING");
        assertThat(result.accessToken()).isEqualTo("token-1");
        verify(otpService).verify("81234567890", "device-1", "OTP_1", "123456");
    }

    @Test
    void rejectsInvalidMobile() {
        assertThatThrownBy(() -> authApplicationService.checkMobile("08123", "device-1"))
                .isInstanceOf(AppBusinessException.class)
                .extracting(ex -> ((AppBusinessException) ex).code())
                .isEqualTo(AppErrorCodes.INVALID_REQUEST);
    }
}
