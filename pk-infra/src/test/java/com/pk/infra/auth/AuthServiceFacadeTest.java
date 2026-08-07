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
import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.auth.OtpChallenge;
import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.OtpChallengeStore;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSendLogRepository.SmsSendLogEntry;
import com.pk.core.auth.port.SmsSender;
import com.pk.core.auth.port.TokenIssuer;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.WhatsAppSendLogRepository;
import com.pk.core.auth.port.WhatsAppSendLogRepository.WhatsAppSendLogEntry;
import com.pk.core.auth.port.WhatsAppSender;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuthServiceFacadeTest {
    private OtpChallengeStore otpChallengeStore;
    private OtpChallengeStore whatsappOtpChallengeStore;
    private UserAuthRepository userAuthRepository;
    private SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private SmsSendLogRepository smsSendLogRepository;
    private SmsSender smsSender;
    private WhatsAppSendLogRepository whatsAppSendLogRepository;
    private WhatsAppSender whatsAppSender;
    private WhatsAppConfigLoader whatsAppConfigLoader;
    private UserProfileBindingRepository userProfileBindingRepository;
    private AppsFlyerS2sReporter appsFlyerS2sReporter;
    private AuthOtpConfigLoader authOtpConfigLoader;
    private SmsConfigLoader smsConfigLoader;
    private AuthServiceFacade facade;

    @BeforeEach
    void setUp() {
        otpChallengeStore = mock(OtpChallengeStore.class);
        whatsappOtpChallengeStore = mock(OtpChallengeStore.class);
        userAuthRepository = mock(UserAuthRepository.class);
        sensitiveFieldEncryptor = mock(SensitiveFieldEncryptor.class);
        smsSendLogRepository = mock(SmsSendLogRepository.class);
        smsSender = mock(SmsSender.class);
        whatsAppSendLogRepository = mock(WhatsAppSendLogRepository.class);
        whatsAppSender = mock(WhatsAppSender.class);
        whatsAppConfigLoader = mock(WhatsAppConfigLoader.class);
        userProfileBindingRepository = mock(UserProfileBindingRepository.class);
        appsFlyerS2sReporter = mock(AppsFlyerS2sReporter.class);
        when(appsFlyerS2sReporter.reportPlatformEvent(any(), any(Long.class), any(), any(), any(), any()))
                .thenReturn(AppsFlyerS2sReporter.ReportResult.recorded(1L, "OK"));
        authOtpConfigLoader = defaultOtpConfigLoader();
        smsConfigLoader = defaultSmsConfigLoader(true, "1234", List.of());
        AuthProperties properties = new AuthProperties();
        properties.setOtpTtl(Duration.ofMinutes(5));
        facade = newFacade(properties, mock(SessionStore.class), mock(RefreshTokenStore.class), mock(TokenIssuer.class));
        when(smsSendLogRepository.countSince(eq("8123456789"), any(Instant.class))).thenReturn(0L);
        when(smsSendLogRepository.insert(any())).thenReturn(1L);
        when(smsSender.send(eq("8123456789"), any())).thenReturn(SmsSendResult.success("local", "local-1"));
        when(whatsAppSendLogRepository.countSince(eq("8123456789"), any(Instant.class))).thenReturn(0L);
        when(whatsAppSendLogRepository.insert(any())).thenReturn(1L);
        when(whatsAppSender.send(eq("8123456789"), any())).thenReturn(SmsSendResult.success("chuanglan", "wa-1"));
        when(whatsAppConfigLoader.loadDailyLimit()).thenReturn(5);
        when(whatsAppConfigLoader.loadConf()).thenReturn(defaultWhatsAppConf());
    }

    private static WhatsAppConfigLoader.WhatsAppConf defaultWhatsAppConf() {
        return new WhatsAppConfigLoader.WhatsAppConf(
                false,
                "https://api.innopaas.com/api/whatsapp/v3",
                "",
                "",
                "",
                "",
                "group_otp_test",
                "id",
                "62",
                Duration.ofSeconds(10),
                Duration.ofSeconds(60),
                Duration.ofSeconds(300),
                "1234",
                List.of()
        );
    }

    private AuthServiceFacade newFacade(
            AuthProperties properties,
            SessionStore sessionStore,
            RefreshTokenStore refreshTokenStore,
            TokenIssuer tokenIssuer
    ) {
        return new AuthServiceFacade(
                properties,
                authOtpConfigLoader,
                smsConfigLoader,
                sessionStore,
                otpChallengeStore,
                whatsappOtpChallengeStore,
                refreshTokenStore,
                tokenIssuer,
                userAuthRepository,
                sensitiveFieldEncryptor,
                smsSendLogRepository,
                smsSender,
                whatsAppSendLogRepository,
                whatsAppSender,
                whatsAppConfigLoader,
                userProfileBindingRepository,
                appsFlyerS2sReporter
        );
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

    private static SmsConfigLoader defaultSmsConfigLoader(
            boolean enableSms,
            String defaultCode,
            List<String> userList
    ) {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        String usersJson = userList.stream()
                .map(u -> "\"" + u + "\"")
                .reduce((a, b) -> a + "," + b)
                .map(s -> "[" + s + "]")
                .orElse("[]");
        when(repository.findByKey(SmsConfigLoader.CONF_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        SmsConfigLoader.CONF_KEY,
                        """
                        {"enableSms":%s,"url":"https://example.com","spid":"s","pwd":"p",
                         "commercialCode":"0062","expireTime":300,"timeout":10000,
                         "defaultCode":"%s","codeLength":4,"userList":%s}
                        """.formatted(enableSms, defaultCode, usersJson)
                )
        ));
        return new SmsConfigLoader(repository, new ObjectMapper());
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
    void rejectsWhatsAppResendForSameDeviceWithinInterval() {
        when(whatsappOtpChallengeStore.timeUntilResendAllowed("device-1"))
                .thenReturn(Optional.of(Duration.ofSeconds(30)));

        assertThatThrownBy(() -> facade.sendWhatsAppCode("8123456789", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.TOO_MANY_REQUESTS);

        verify(whatsappOtpChallengeStore, never()).save(any(), any(), any());
        verify(whatsAppSendLogRepository, never()).insert(any());
    }

    @Test
    void sendsWhatsAppAndPersistsSendLog() {
        when(whatsappOtpChallengeStore.timeUntilResendAllowed("device-1")).thenReturn(Optional.empty());

        var result = facade.sendWhatsAppCode("8123456789", "device-1");

        assertThat(result.otpToken()).isNotBlank();
        ArgumentCaptor<WhatsAppSendLogEntry> logCaptor = ArgumentCaptor.forClass(WhatsAppSendLogEntry.class);
        verify(whatsAppSendLogRepository).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().otpToken()).isEqualTo(result.otpToken());
        verify(whatsAppSender).send(eq("8123456789"), any());
        verify(whatsappOtpChallengeStore).markSent(eq("device-1"), eq(Duration.ofSeconds(60)));
        verify(smsSender, never()).send(any(), any());
    }

    @Test
    void rejectsWhatsAppSendWhenDailyLimitReached() {
        when(whatsappOtpChallengeStore.timeUntilResendAllowed("device-1")).thenReturn(Optional.empty());
        when(whatsAppConfigLoader.loadDailyLimit()).thenReturn(5);
        when(whatsAppSendLogRepository.countSince(eq("8123456789"), any(Instant.class))).thenReturn(5L);

        assertThatThrownBy(() -> facade.sendWhatsAppCode("8123456789", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.TOO_MANY_REQUESTS);

        verify(whatsappOtpChallengeStore, never()).save(any(), any(), any());
        verify(whatsAppSendLogRepository, never()).insert(any());
    }

    @Test
    void rollsBackWhatsAppChallengeWhenProviderFails() {
        when(whatsappOtpChallengeStore.timeUntilResendAllowed("device-1")).thenReturn(Optional.empty());
        when(whatsAppSender.send(eq("8123456789"), any()))
                .thenReturn(SmsSendResult.failure("chuanglan", "TIMEOUT", "gateway timeout"));

        assertThatThrownBy(() -> facade.sendWhatsAppCode("8123456789", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.SERVICE_UNAVAILABLE);

        verify(whatsappOtpChallengeStore).delete(any());
        verify(whatsappOtpChallengeStore, never()).markSent(any(), any());
    }

    @Test
    void persistsSessionTokensAfterSuccessfulOtpVerify() {
        when(otpChallengeStore.timeUntilResendAllowed("device-1")).thenReturn(Optional.empty());
        when(otpChallengeStore.findByToken("token-1")).thenReturn(Optional.of(
                new OtpChallenge("8123456789", "device-1", "123456", Instant.now().plusSeconds(300))
        ));
        when(userAuthRepository.findOrCreateActiveByMobileNo("8123456789"))
                .thenReturn(new UserProfileSummary(7L, "UABC", "8123456789", false));
        when(userAuthRepository.isPasswordSet(7L)).thenReturn(false);

        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        properties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        when(sessionStore.findByUserId(7L)).thenReturn(Optional.empty());

        AuthServiceFacade verifyFacade = newFacade(properties, sessionStore, refreshTokenStore, tokenIssuer);

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
        verify(userAuthRepository).updateLastLoginAt(eq(7L), any(Instant.class));
    }

    @Test
    void loginWithWhatsAppCreatesUserWhenMissing() {
        when(whatsappOtpChallengeStore.findTokenByMobile("8123456789")).thenReturn(Optional.of("wa-token"));
        when(whatsappOtpChallengeStore.findByToken("wa-token")).thenReturn(Optional.of(
                new OtpChallenge("8123456789", "device-1", "654321", Instant.now().plusSeconds(300))
        ));
        when(userAuthRepository.findOrCreateActiveByMobileNo("8123456789"))
                .thenReturn(new UserProfileSummary(9L, "UWA", "8123456789", true));
        when(userAuthRepository.isPasswordSet(9L)).thenReturn(false);

        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        properties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        when(sessionStore.findByUserId(9L)).thenReturn(Optional.empty());

        AuthServiceFacade verifyFacade = newFacade(properties, sessionStore, refreshTokenStore, tokenIssuer);

        AuthServiceFacade.OtpVerifyResult result = verifyFacade.loginWithWhatsApp(
                "8123456789",
                "654321",
                "device-1",
                "ios"
        );

        assertThat(result.profile().newlyCreated()).isTrue();
        assertThat(result.tokenPair().accessToken()).isNotBlank();
        verify(whatsappOtpChallengeStore).delete("wa-token");
        verify(otpChallengeStore, never()).findByToken(any());
        verify(userAuthRepository).findOrCreateActiveByMobileNo("8123456789");
        verify(userAuthRepository, never()).createByMobileNo(any());
        verify(userAuthRepository).updateLastLoginAt(eq(9L), any(Instant.class));
        verify(appsFlyerS2sReporter).reportPlatformEvent(
                eq(AppsFlyerS2sReporter.EVENT_REGISTER_SUCCESS_PK),
                eq(9L),
                eq("UWA"),
                eq("device-1"),
                eq("ios"),
                eq(null)
        );
    }

    @Test
    void logoutClearsSessionAndRecordsLastLogoutAt() {
        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        properties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        AuthServiceFacade logoutFacade = newFacade(properties, sessionStore, refreshTokenStore, tokenIssuer);

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(11L, "UP11", "8123456789", 3L);
        logoutFacade.logout(principal);

        verify(sessionStore).delete(11L);
        verify(refreshTokenStore).deleteAllForProfile(11L);
        verify(userAuthRepository).clearSessionTokens(11L);
        verify(userAuthRepository).updateLastLogoutAt(eq(11L), any(Instant.class));
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
    void acceptsDefaultCodeWhenSmsDisabledWithoutChallenge() {
        // Dev/test mock: enableSms=false → any mobile may use smsConf.defaultCode
        smsConfigLoader = defaultSmsConfigLoader(false, "1234", List.of());
        AuthProperties properties = verifyProperties();
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        when(sessionStore.findByUserId(7L)).thenReturn(Optional.empty());
        when(otpChallengeStore.findByToken("unused-token")).thenReturn(Optional.empty());
        when(userAuthRepository.findOrCreateActiveByMobileNo("8123456789"))
                .thenReturn(new UserProfileSummary(7L, "UABC", "8123456789", false));
        when(userAuthRepository.isPasswordSet(7L)).thenReturn(false);

        AuthServiceFacade verifyFacade = newFacade(properties, sessionStore, refreshTokenStore, tokenIssuer);

        AuthServiceFacade.OtpVerifyResult result = verifyFacade.verifyOtp(
                "8123456789",
                "unused-token",
                "1234",
                "device-1"
        );

        assertThat(result.tokenPair().accessToken()).isNotBlank();
        verify(otpChallengeStore, never()).delete(any());
    }

    @Test
    void rejectsDefaultCodeForNonWhitelistWhenSmsEnabled() {
        smsConfigLoader = defaultSmsConfigLoader(true, "2460", List.of("8999999999"));
        AuthProperties properties = verifyProperties();
        AuthServiceFacade verifyFacade = newFacade(
                properties,
                mock(SessionStore.class),
                mock(RefreshTokenStore.class),
                new JwtTokenIssuer(properties)
        );
        when(otpChallengeStore.findByToken("token-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyFacade.verifyOtp("8123456789", "token-1", "2460", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
    }

    @Test
    void rejectsUnknownCodeWhenSmsEnabledAndNotOnWhitelist() {
        when(otpChallengeStore.findByToken("token-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.verifyOtp("8123456789", "token-1", "123456", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
    }

    @Test
    void acceptsWhitelistDefaultCodeWithoutStoredChallenge() {
        smsConfigLoader = defaultSmsConfigLoader(true, "1234", List.of("8123456789"));
        AuthProperties properties = verifyProperties();
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        when(sessionStore.findByUserId(7L)).thenReturn(Optional.empty());
        when(otpChallengeStore.findByToken("unused-token")).thenReturn(Optional.empty());
        when(userAuthRepository.findOrCreateActiveByMobileNo("8123456789"))
                .thenReturn(new UserProfileSummary(7L, "UABC", "8123456789", false));
        when(userAuthRepository.isPasswordSet(7L)).thenReturn(false);

        AuthServiceFacade verifyFacade = newFacade(properties, sessionStore, refreshTokenStore, tokenIssuer);

        AuthServiceFacade.OtpVerifyResult result = verifyFacade.verifyOtp(
                "8123456789",
                "unused-token",
                "1234",
                "device-1"
        );

        assertThat(result.tokenPair().accessToken()).isNotBlank();
        verify(otpChallengeStore, never()).delete(any());
    }

    @Test
    void rejectsDefaultCodeWhenMobileNotOnWhitelist() {
        smsConfigLoader = defaultSmsConfigLoader(true, "1234", List.of("8999999999"));
        AuthProperties properties = verifyProperties();
        AuthServiceFacade verifyFacade = newFacade(
                properties,
                mock(SessionStore.class),
                mock(RefreshTokenStore.class),
                new JwtTokenIssuer(properties)
        );
        when(otpChallengeStore.findByToken("token-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyFacade.verifyOtp("8123456789", "token-1", "1234", "device-1"))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE);
    }

    @Test
    void acceptsDefaultCodeForEveryoneWhenSmsDisabled() {
        smsConfigLoader = defaultSmsConfigLoader(false, "1234", List.of());
        AuthProperties properties = verifyProperties();
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        when(sessionStore.findByUserId(7L)).thenReturn(Optional.empty());
        when(otpChallengeStore.findByToken("unused-token")).thenReturn(Optional.empty());
        when(userAuthRepository.findOrCreateActiveByMobileNo("8123456789"))
                .thenReturn(new UserProfileSummary(7L, "UABC", "8123456789", false));
        when(userAuthRepository.isPasswordSet(7L)).thenReturn(false);

        AuthServiceFacade verifyFacade = newFacade(properties, sessionStore, refreshTokenStore, tokenIssuer);

        AuthServiceFacade.OtpVerifyResult result = verifyFacade.verifyOtp(
                "8123456789",
                "unused-token",
                "1234",
                "device-1"
        );

        assertThat(result.tokenPair().accessToken()).isNotBlank();
    }

    @Test
    void acceptsWhatsAppDefaultCodeWhenWhatsAppDisabled() {
        when(whatsAppConfigLoader.loadConf()).thenReturn(defaultWhatsAppConf());
        AuthProperties properties = verifyProperties();
        TokenIssuer tokenIssuer = new JwtTokenIssuer(properties);
        SessionStore sessionStore = mock(SessionStore.class);
        RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
        when(sessionStore.findByUserId(7L)).thenReturn(Optional.empty());
        when(whatsappOtpChallengeStore.findTokenByMobile("8123456789")).thenReturn(Optional.empty());
        when(userAuthRepository.findOrCreateActiveByMobileNo("8123456789"))
                .thenReturn(new UserProfileSummary(7L, "UWA", "8123456789", false));
        when(userAuthRepository.isPasswordSet(7L)).thenReturn(false);

        AuthServiceFacade verifyFacade = newFacade(properties, sessionStore, refreshTokenStore, tokenIssuer);

        AuthServiceFacade.OtpVerifyResult result = verifyFacade.loginWithWhatsApp(
                "8123456789",
                "1234",
                "device-1"
        );

        assertThat(result.tokenPair().accessToken()).isNotBlank();
        verify(whatsappOtpChallengeStore, never()).findByToken(any());
    }

    private static AuthProperties verifyProperties() {
        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        properties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");
        return properties;
    }
}
