package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.appconfig.port.AppConfigRepository;
import com.pk.core.auth.OtpChallenge;
import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.OtpChallengeStore;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSendLogRepository.SmsSendLogEntry;
import com.pk.core.auth.port.SmsSender;
import com.pk.core.auth.port.TokenIssuer;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuthServiceFacadeTest {
    private OtpChallengeStore otpChallengeStore;
    private UserAuthRepository userAuthRepository;
    private SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private SmsSendLogRepository smsSendLogRepository;
    private SmsSender smsSender;
    private AuthOtpConfigLoader authOtpConfigLoader;
    private AuthServiceFacade facade;

    @BeforeEach
    void setUp() {
        otpChallengeStore = mock(OtpChallengeStore.class);
        userAuthRepository = mock(UserAuthRepository.class);
        sensitiveFieldEncryptor = mock(SensitiveFieldEncryptor.class);
        smsSendLogRepository = mock(SmsSendLogRepository.class);
        smsSender = mock(SmsSender.class);
        authOtpConfigLoader = defaultOtpConfigLoader();
        AuthProperties properties = new AuthProperties();
        properties.setOtpTtl(Duration.ofMinutes(5));
        facade = new AuthServiceFacade(
                properties,
                authOtpConfigLoader,
                mock(SessionStore.class),
                otpChallengeStore,
                mock(RefreshTokenStore.class),
                mock(TokenIssuer.class),
                userAuthRepository,
                sensitiveFieldEncryptor,
                smsSendLogRepository,
                smsSender
        );
        when(smsSendLogRepository.countSince(eq("8123456789"), any(Instant.class))).thenReturn(0L);
        when(smsSendLogRepository.insert(any())).thenReturn(1L);
        when(smsSender.send(eq("8123456789"), any())).thenReturn(SmsSendResult.success("local", "local-1"));
    }

    private static AuthOtpConfigLoader defaultOtpConfigLoader() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(AuthOtpConfigLoader.CONFIG_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        AuthOtpConfigLoader.CONFIG_KEY,
                        """
                        {"otpDailyLimit":10,"otpResendIntervalSeconds":60,"otpDailyLimitZone":"Asia/Jakarta"}
                        """
                )
        ));
        return new AuthOtpConfigLoader(repository, new ObjectMapper());
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
        ArgumentCaptor<SmsSendLogEntry> logCaptor = ArgumentCaptor.forClass(SmsSendLogEntry.class);
        verify(smsSendLogRepository).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().otpToken()).isEqualTo(result.otpToken());
        verify(smsSender).send(eq("8123456789"), any());
    }

    @Test
    void persistsSessionTokensAfterSuccessfulOtpVerify() {
        when(otpChallengeStore.timeUntilResendAllowed("device-1")).thenReturn(Optional.empty());
        when(otpChallengeStore.findByToken("token-1")).thenReturn(Optional.of(
                new OtpChallenge("8123456789", "device-1", "123456", Instant.now().plusSeconds(300))
        ));
        when(userAuthRepository.findByMobileNo("8123456789"))
                .thenReturn(Optional.of(new UserProfileSummary(7L, "UABC", "8123456789", false)));
        when(userAuthRepository.isPasswordSet(7L)).thenReturn(false);

        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        properties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        when(sessionStore.findByProfileId(7L)).thenReturn(Optional.empty());

        AuthServiceFacade verifyFacade = new AuthServiceFacade(
                properties,
                defaultOtpConfigLoader(),
                sessionStore,
                otpChallengeStore,
                refreshTokenStore,
                tokenIssuer,
                userAuthRepository,
                sensitiveFieldEncryptor,
                smsSendLogRepository,
                smsSender
        );

        AuthServiceFacade.OtpVerifyResult result = verifyFacade.verifyOtp(
                "8123456789",
                "token-1",
                "123456",
                "device-1"
        );

        assertThat(result.tokenPair().accessToken()).isNotBlank();
        ArgumentCaptor<Instant> expiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(userAuthRepository).saveSessionTokens(
                eq(7L),
                eq(result.tokenPair().accessToken()),
                eq(result.tokenPair().refreshToken()),
                expiresAtCaptor.capture()
        );
        assertThat(expiresAtCaptor.getValue())
                .isAfter(Instant.now().plusSeconds(890))
                .isBefore(Instant.now().plusSeconds(910));
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
        when(userAuthRepository.isPasswordSet(1L)).thenReturn(true);

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
        when(userAuthRepository.isPasswordSet(1L)).thenReturn(false);
        EncryptedField encrypted = new EncryptedField("cipher", new byte[12], new byte[16]);
        when(sensitiveFieldEncryptor.encrypt("abc123")).thenReturn(encrypted);

        facade.setPassword(1L, "abc123", "abc123");

        verify(userAuthRepository).savePassword(1L, encrypted);
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
                .isEqualTo(ApiCode.INVALID_MOBILE_NUMBER);

        verify(userAuthRepository, never()).findByMobileNo(any());
    }

    @Test
    void rejectsMobileNotStartingWithEight() {
        assertThatThrownBy(() -> facade.sendOtp("123456783", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_MOBILE_NUMBER);

        verify(otpChallengeStore, never()).save(any(), any(), any());
        verify(smsSendLogRepository, never()).insert(any());
    }

    @Test
    void acceptsBypassCodeWithoutStoredChallengeWhenEnabled() {
        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        properties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");
        properties.setOtpBypassEnabled(true);
        properties.setOtpBypassCode("123456");
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        when(sessionStore.findByProfileId(7L)).thenReturn(Optional.empty());
        when(otpChallengeStore.findByToken("unused-token")).thenReturn(Optional.empty());
        when(userAuthRepository.findByMobileNo("8123456789"))
                .thenReturn(Optional.of(new UserProfileSummary(7L, "UABC", "8123456789", false)));
        when(userAuthRepository.isPasswordSet(7L)).thenReturn(false);

        AuthServiceFacade verifyFacade = new AuthServiceFacade(
                properties,
                defaultOtpConfigLoader(),
                sessionStore,
                otpChallengeStore,
                refreshTokenStore,
                tokenIssuer,
                userAuthRepository,
                sensitiveFieldEncryptor,
                smsSendLogRepository,
                smsSender
        );

        AuthServiceFacade.OtpVerifyResult result = verifyFacade.verifyOtp(
                "8123456789",
                "unused-token",
                "123456",
                "device-1"
        );

        assertThat(result.tokenPair().accessToken()).isNotBlank();
        verify(otpChallengeStore, never()).delete(any());
    }

    @Test
    void rejectsBypassCodeWhenBypassDisabled() {
        when(otpChallengeStore.findByToken("token-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.verifyOtp("8123456789", "token-1", "123456", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
    }
}
