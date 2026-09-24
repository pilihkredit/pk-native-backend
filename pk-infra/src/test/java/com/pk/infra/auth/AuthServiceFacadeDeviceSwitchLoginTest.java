package com.pk.infra.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.pk.core.auth.AuthSession;
import com.pk.core.auth.TokenPair;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.DeviceSwitchFaceVerificationRepository;
import com.pk.core.auth.port.OtpChallengeStore;
import com.pk.core.auth.port.RefreshTokenStore;
import com.pk.core.auth.port.SessionStore;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSender;
import com.pk.core.auth.port.TokenIssuer;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.WhatsAppSendLogRepository;
import com.pk.core.auth.port.WhatsAppSender;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.infra.profile.FaceComparisonBaselineResolver;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Exercises AuthServiceFacade with a real {@link LoginDeviceSwitchGateService} (not a noop mock). */
class AuthServiceFacadeDeviceSwitchLoginTest {
    private UserAuthRepository userAuthRepository;
    private SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private FaceComparisonBaselineResolver baselineResolver;
    private DeviceSwitchFaceVerificationRepository deviceSwitchFaceRepository;
    private SessionStore sessionStore;
    private RefreshTokenStore refreshTokenStore;
    private TokenIssuer tokenIssuer;
    private AuthServiceFacade facade;

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        sensitiveFieldEncryptor = mock(SensitiveFieldEncryptor.class);
        baselineResolver = mock(FaceComparisonBaselineResolver.class);
        deviceSwitchFaceRepository = mock(DeviceSwitchFaceVerificationRepository.class);
        sessionStore = mock(SessionStore.class);
        refreshTokenStore = mock(RefreshTokenStore.class);
        LoginDeviceSwitchGateService gate = new LoginDeviceSwitchGateService(
                userAuthRepository,
                baselineResolver,
                deviceSwitchFaceRepository
        );
        AuthProperties properties = new AuthProperties();
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(30));
        properties.setJwtSecret("local-dev-secret-change-in-prod-min-32-chars");
        tokenIssuer = new JwtTokenIssuer(properties);
        AppsFlyerS2sReporter appsFlyerS2sReporter = mock(AppsFlyerS2sReporter.class);
        when(appsFlyerS2sReporter.reportPlatformEvent(any(), anyLong(), any(), any(), any(), any()))
                .thenReturn(AppsFlyerS2sReporter.ReportResult.recorded(1L, "OK"));
        facade = new AuthServiceFacade(
                properties,
                defaultOtpConfigLoader(),
                defaultSmsConfigLoader(),
                sessionStore,
                mock(OtpChallengeStore.class),
                mock(OtpChallengeStore.class),
                refreshTokenStore,
                tokenIssuer,
                userAuthRepository,
                sensitiveFieldEncryptor,
                mock(SmsSendLogRepository.class),
                mock(SmsSender.class),
                mock(WhatsAppSendLogRepository.class),
                mock(WhatsAppSender.class),
                mock(WhatsAppConfigLoader.class),
                mock(UserProfileBindingRepository.class),
                appsFlyerS2sReporter,
                gate
        );
        when(sessionStore.findByUserId(1L)).thenReturn(Optional.empty());
    }

    @Test
    void mobileCheckRequiresFaceWhenBaselineExistsAndDeviceChanged() {
        when(userAuthRepository.findByMobileNo("8123456789"))
                .thenReturn(Optional.of(new UserProfileSummary(1L, "U1", "8123456789", false)));
        when(userAuthRepository.isPasswordSet(1L)).thenReturn(true);
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("device-old"));

        AuthServiceFacade.MobileCheckResult result =
                facade.checkMobileRegistration("8123456789", "device-new");

        assertThat(result.faceRequired()).isTrue();
    }

    @Test
    void mobileCheckDoesNotRequireFaceOnSameDevice() {
        when(userAuthRepository.findByMobileNo("8123456789"))
                .thenReturn(Optional.of(new UserProfileSummary(1L, "U1", "8123456789", false)));
        when(userAuthRepository.isPasswordSet(1L)).thenReturn(true);
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("device-a"));

        AuthServiceFacade.MobileCheckResult result =
                facade.checkMobileRegistration("8123456789", "device-a");

        assertThat(result.faceRequired()).isFalse();
    }

    @Test
    void passwordLoginRejectsWhenFaceRequiredAndTokenMissing() {
        stubPasswordLoginPrerequisites();
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("device-old"));

        assertThatThrownBy(() -> facade.loginByPassword("8123456789", "Secret123", "device-new", null))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);

        verify(sessionStore, never()).save(anyLong(), any(), any());
        verify(deviceSwitchFaceRepository, never()).consume(any(), any());
    }

    @Test
    void passwordLoginConsumesTokenBeforeSessionAndUpdatesDeviceAfter() {
        stubPasswordLoginPrerequisites();
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("device-old"));
        Instant future = Instant.now().plusSeconds(300);
        when(deviceSwitchFaceRepository.findByToken("face-tok"))
                .thenReturn(Optional.of(faceTicket(1L, "device-new", "face-tok", future)));
        when(deviceSwitchFaceRepository.consume(eq("face-tok"), any(Instant.class))).thenReturn(true);

        AuthServiceFacade.PasswordLoginResult result = facade.loginByPassword(
                "8123456789",
                "Secret123",
                "device-new",
                "face-tok"
        );

        assertThat(result.tokenPair().accessToken()).isNotBlank();
        verify(deviceSwitchFaceRepository).consume(eq("face-tok"), any(Instant.class));
        verify(sessionStore).save(eq(1L), any(AuthSession.class), any());
        verify(userAuthRepository).updateLastLoginDeviceNo(1L, "device-new");
    }

    @Test
    void passwordLoginRejectsTokenReplayWithoutOpeningSession() {
        stubPasswordLoginPrerequisites();
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("device-old"));
        Instant future = Instant.now().plusSeconds(300);
        when(deviceSwitchFaceRepository.findByToken("face-tok"))
                .thenReturn(Optional.of(faceTicket(1L, "device-new", "face-tok", future)));
        when(deviceSwitchFaceRepository.consume(eq("face-tok"), any(Instant.class))).thenReturn(false);

        assertThatThrownBy(() -> facade.loginByPassword(
                "8123456789",
                "Secret123",
                "device-new",
                "face-tok"
        ))
                .extracting("apiCode")
                .isEqualTo(ApiCode.LOGIN_FACE_VERIFICATION_REQUIRED);

        verify(sessionStore, never()).save(anyLong(), any(), any());
        verify(userAuthRepository, never()).updateLastLoginDeviceNo(anyLong(), any());
    }

    @Test
    void passwordLoginSkipsFaceGateWhenSameDeviceAsLastLogin() {
        stubPasswordLoginPrerequisites();
        when(baselineResolver.hasComparableBaseline(1L)).thenReturn(true);
        when(userAuthRepository.findLastLoginDeviceNo(1L)).thenReturn(Optional.of("device-a"));

        AuthServiceFacade.PasswordLoginResult result =
                facade.loginByPassword("8123456789", "Secret123", "device-a", null);

        assertThat(result.tokenPair().accessToken()).isNotBlank();
        verify(deviceSwitchFaceRepository, never()).consume(any(), any());
        verify(userAuthRepository).updateLastLoginDeviceNo(1L, "device-a");
    }

    private void stubPasswordLoginPrerequisites() {
        when(userAuthRepository.findByMobileNo("8123456789"))
                .thenReturn(Optional.of(new UserProfileSummary(1L, "U1", "8123456789", false)));
        EncryptedField encrypted = new EncryptedField("cipher", new byte[12], new byte[16]);
        when(userAuthRepository.findPasswordCredential(1L))
                .thenReturn(Optional.of(new UserAuthRepository.PasswordCredential(1L, encrypted)));
        when(sensitiveFieldEncryptor.decrypt(encrypted)).thenReturn("Secret123");
    }

    private static DeviceSwitchFaceVerificationRepository.FaceVerificationData faceTicket(
            long userId,
            String deviceNo,
            String token,
            Instant expiresAt
    ) {
        return new DeviceSwitchFaceVerificationRepository.FaceVerificationData(
                1L,
                token,
                userId,
                "U1",
                "req-1",
                deviceNo,
                "live-1",
                "IDENTITY",
                90D,
                "VERIFIED_PENDING",
                expiresAt,
                null
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

    private static SmsConfigLoader defaultSmsConfigLoader() {
        AppConfigRepository repository = mock(AppConfigRepository.class);
        when(repository.findByKey(SmsConfigLoader.CONF_KEY)).thenReturn(Optional.of(
                new AppConfigRepository.AppConfigRecord(
                        1L,
                        SmsConfigLoader.CONF_KEY,
                        """
                        {"enableSms":true,"url":"https://example.com","spid":"s","pwd":"p",
                         "commercialCode":"0062","expireTime":300,"timeout":10000,
                         "defaultCode":"1234","codeLength":4,"userList":[]}
                        """
                )
        ));
        return new SmsConfigLoader(repository, new ObjectMapper());
    }
}
