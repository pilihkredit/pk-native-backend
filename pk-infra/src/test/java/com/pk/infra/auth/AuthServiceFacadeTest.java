package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.OtpChallengeStore;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSender;
import com.pk.core.auth.port.TokenIssuer;
import com.pk.core.auth.port.PasswordHasher;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.UserPasswordCredentialRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceFacadeTest {
    private OtpChallengeStore otpChallengeStore;
    private UserAuthRepository userAuthRepository;
    private UserPasswordCredentialRepository userPasswordCredentialRepository;
    private PasswordHasher passwordHasher;
    private SmsSendLogRepository smsSendLogRepository;
    private SmsSender smsSender;
    private AuthServiceFacade facade;

    @BeforeEach
    void setUp() {
        otpChallengeStore = mock(OtpChallengeStore.class);
        userAuthRepository = mock(UserAuthRepository.class);
        userPasswordCredentialRepository = mock(UserPasswordCredentialRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        smsSendLogRepository = mock(SmsSendLogRepository.class);
        smsSender = mock(SmsSender.class);
        AuthProperties properties = new AuthProperties();
        properties.setOtpTtl(Duration.ofMinutes(5));
        properties.setOtpResendInterval(Duration.ofSeconds(60));
        properties.setOtpDailyLimit(10);
        properties.setOtpDailyLimitZone(ZoneId.of("Asia/Jakarta"));
        facade = new AuthServiceFacade(
                properties,
                mock(SessionStore.class),
                otpChallengeStore,
                mock(RefreshTokenStore.class),
                mock(TokenIssuer.class),
                userAuthRepository,
                userPasswordCredentialRepository,
                passwordHasher,
                smsSendLogRepository,
                smsSender
        );
        when(smsSendLogRepository.countSince(eq("8123456789"), any(Instant.class))).thenReturn(0L);
        when(smsSendLogRepository.insert(any())).thenReturn(1L);
        when(smsSender.send(eq("8123456789"), any())).thenReturn(SmsSendResult.success("local", "local-1"));
    }

    @Test
    void rejectsOtpResendForSameDeviceWithinInterval() {
        when(otpChallengeStore.timeUntilResendAllowed("device-1"))
                .thenReturn(Optional.of(Duration.ofSeconds(30)));

        assertThatThrownBy(() -> facade.sendOtp("8123456789", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.TOO_MANY_REQUESTS);

        verify(otpChallengeStore, never()).save(any(), any(), any());
        verify(smsSendLogRepository, never()).insert(any());
    }

    @Test
    void allowsOtpSendForDifferentDeviceWhileAnotherDeviceIsInCooldown() {
        when(otpChallengeStore.timeUntilResendAllowed("device-1"))
                .thenReturn(Optional.of(Duration.ofSeconds(30)));
        when(otpChallengeStore.timeUntilResendAllowed("device-2"))
                .thenReturn(Optional.empty());

        var result = facade.sendOtp("8123456789", "device-2");

        assertThat(result.otpToken()).isNotBlank();
        verify(otpChallengeStore).markSent(eq("device-2"), eq(Duration.ofSeconds(60)));
        verify(smsSendLogRepository).insert(any());
        verify(smsSender).send(eq("8123456789"), any());
    }

    @Test
    void rejectsOtpSendWhenDailyLimitReached() {
        when(otpChallengeStore.timeUntilResendAllowed("device-1")).thenReturn(Optional.empty());
        when(smsSendLogRepository.countSince(eq("8123456789"), any(Instant.class))).thenReturn(10L);

        assertThatThrownBy(() -> facade.sendOtp("8123456789", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.TOO_MANY_REQUESTS);

        verify(otpChallengeStore, never()).save(any(), any(), any());
        verify(smsSendLogRepository, never()).insert(any());
    }

    @Test
    void rollsBackOtpChallengeWhenSmsProviderFails() {
        when(otpChallengeStore.timeUntilResendAllowed("device-1")).thenReturn(Optional.empty());
        when(smsSender.send(eq("8123456789"), any()))
                .thenReturn(SmsSendResult.failure("vendor", "TIMEOUT", "gateway timeout"));

        assertThatThrownBy(() -> facade.sendOtp("8123456789", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);

        verify(otpChallengeStore).delete(any());
        verify(otpChallengeStore, never()).markSent(any(), any());
    }

    @Test
    void returnsExistingWhenMobileIsRegistered() {
        when(userAuthRepository.findByMobileNo("8123456789"))
                .thenReturn(Optional.of(new UserProfileSummary(1L, "UABC", "8123456789", false)));
        when(userPasswordCredentialRepository.isPasswordSet(1L)).thenReturn(true);

        var result = facade.checkMobileRegistration("8123456789", "device-1");

        assertThat(result.registered()).isTrue();
        assertThat(result.accountStatus()).isEqualTo("EXISTING");
        assertThat(result.passwordSet()).isTrue();
    }

    @Test
    void returnsNewWhenMobileIsNotRegistered() {
        when(userAuthRepository.findByMobileNo("8123456789")).thenReturn(Optional.empty());

        var result = facade.checkMobileRegistration("8123456789", "device-1");

        assertThat(result.registered()).isFalse();
        assertThat(result.accountStatus()).isEqualTo("NEW");
        assertThat(result.passwordSet()).isFalse();
    }

    @Test
    void setsPasswordWhenFormatIsValid() {
        when(userPasswordCredentialRepository.isPasswordSet(1L)).thenReturn(false);
        when(passwordHasher.hash("abc123")).thenReturn("hashed");

        facade.setPassword(1L, "abc123", "abc123");

        verify(userPasswordCredentialRepository).insert(1L, "hashed");
    }

    @Test
    void rejectsPasswordWhenConfirmationDoesNotMatch() {
        assertThatThrownBy(() -> facade.setPassword(1L, "abc123", "abc124"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.PASSWORD_CONFIRM_MISMATCH);
    }

    @Test
    void rejectsPasswordWhenFormatIsInvalid() {
        assertThatThrownBy(() -> facade.setPassword(1L, "abcdef", "abcdef"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_PASSWORD_FORMAT);
    }

    @Test
    void rejectsMobileCheckForInvalidMobile() {
        assertThatThrownBy(() -> facade.checkMobileRegistration("+8613812345678", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);

        verify(userAuthRepository, never()).findByMobileNo(any());
    }
}
