package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.MobileChangeOtpChallenge;
import com.pk.core.auth.TokenPair;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.MobileChangeOtpChallengeStore;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.auth.port.UserMobileChangeLogRepository;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.infra.auth.AuthServiceFacade;
import com.pk.infra.auth.SmsConfigLoader;
import com.pk.infra.auth.WhatsAppConfigLoader;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MobileChangeFacadeTest {
    private UserAuthRepository userAuthRepository;
    private UserMobileChangeLogRepository changeLogRepository;
    private MobileChangeFaceVerificationRepository faceRepository;
    private MobileChangeOtpChallengeStore challengeStore;
    private AuthServiceFacade authServiceFacade;
    private SmsConfigLoader smsConfigLoader;
    private WhatsAppConfigLoader whatsAppConfigLoader;
    private ProfileSyncOrchestrator profileSyncOrchestrator;
    private UserDeviceWriter userDeviceWriter;
    private MobileChangeFacade facade;
    private static final LenderDeviceContext DEVICE = new LenderDeviceContext(
            "PilihKredit", "9.0.0", "com.pilihkredit.id", "device-1", "ios");

    @Test
    void otpChallengeCarriesDeliveryChannel() {
        assertThat(Arrays.stream(MobileChangeOtpChallenge.class.getRecordComponents())
                .map(component -> component.getName()))
                .contains("channel");
    }

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        changeLogRepository = mock(UserMobileChangeLogRepository.class);
        faceRepository = mock(MobileChangeFaceVerificationRepository.class);
        challengeStore = mock(MobileChangeOtpChallengeStore.class);
        authServiceFacade = mock(AuthServiceFacade.class);
        smsConfigLoader = mock(SmsConfigLoader.class);
        whatsAppConfigLoader = mock(WhatsAppConfigLoader.class);
        profileSyncOrchestrator = mock(ProfileSyncOrchestrator.class);
        userDeviceWriter = mock(UserDeviceWriter.class);
        when(smsConfigLoader.loadConf()).thenReturn(new SmsConfigLoader.SmsConf(
                true, "url", "spid", "pwd", "0062", "code {code}", 300, 10000,
                "123456", 6, List.of()));
        when(whatsAppConfigLoader.loadConf()).thenReturn(new WhatsAppConfigLoader.WhatsAppConf(
                true, "url", "app-key", "authorization", "waba-id", "send-number", "template",
                "id", "62", java.time.Duration.ofSeconds(10), java.time.Duration.ofSeconds(60),
                java.time.Duration.ofSeconds(300), "246810", List.of("81222222222")));
        facade = new MobileChangeFacade(
                userAuthRepository,
                changeLogRepository,
                faceRepository,
                challengeStore,
                authServiceFacade,
                smsConfigLoader,
                whatsAppConfigLoader,
                profileSyncOrchestrator,
                userDeviceWriter
        );
    }

    @Test
    void verifiesOtpChangesMobilePromotesFaceAndReturnsReplacementSession() {
        Instant expiresAt = Instant.now().plusSeconds(300);
        when(challengeStore.findByToken("otp-token")).thenReturn(Optional.of(
                new MobileChangeOtpChallenge(
                        "otp-token", 1L, "81222222222", "device-1", "face-token", "654321", expiresAt)));
        when(faceRepository.findVerifiedByToken("face-token")).thenReturn(Optional.of(
                new MobileChangeFaceVerificationRepository.FaceVerificationData(
                        10L, "face-token", 1L, "U1", "req-face", "device-1", "live-1",
                        "IDENTITY", "baseline-ref", "candidate-ref", 88D, "VERIFIED_PENDING",
                        "otp-token", expiresAt, null)));
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.empty());
        when(faceRepository.promote(org.mockito.ArgumentMatchers.eq("face-token"),
                org.mockito.ArgumentMatchers.any())).thenReturn(true);
        TokenPair replacement = TokenPair.of("new-access", "new-refresh", 900);
        when(authServiceFacade.openSessionAfterMobileChange(1L, "device-1")).thenReturn(replacement);

        MobileChangeFacade.MobileChangeResult result = facade.verifyAndChange(
                1L,
                new MobileChangeFacade.MobileChangeVerifyCommand(
                        "req-1", "81222222222", "face-token", "otp-token", "654321", "device-1", DEVICE));

        assertThat(result.changed()).isTrue();
        assertThat(result.mobileNo()).isEqualTo("81222222222");
        assertThat(result.tokenPair()).isEqualTo(replacement);
        verify(userAuthRepository).updateMobileNo(1L, "81222222222");
        verify(changeLogRepository).insert(argThat(entry ->
                entry.faceTicketId().equals("face-token") && entry.otpTicketId().equals("otp-token")));
        verify(faceRepository).promote(org.mockito.ArgumentMatchers.eq("face-token"),
                org.mockito.ArgumentMatchers.any());
        verify(challengeStore).delete("otp-token");
        verify(authServiceFacade).openSessionAfterMobileChange(1L, "device-1");
        verify(userDeviceWriter).upsertFromRequest(1L, "U1", "req-1", DEVICE);
        verify(profileSyncOrchestrator).syncNow(argThat(job ->
                job.module() == ProfileSyncModule.MOBILE
                        && job.mobileNo().equals("81222222222")
                        && job.partnerUserId().equals("U1")
                        && job.device().equals(DEVICE)));
    }

    @Test
    void lenderFailurePreventsReplacementSession() {
        Instant expiresAt = Instant.now().plusSeconds(300);
        when(challengeStore.findByToken("otp-token")).thenReturn(Optional.of(
                new MobileChangeOtpChallenge(
                        "otp-token", 1L, "81222222222", "device-1", "face-token", "654321", expiresAt)));
        when(faceRepository.findVerifiedByToken("face-token")).thenReturn(Optional.of(
                new MobileChangeFaceVerificationRepository.FaceVerificationData(
                        10L, "face-token", 1L, "U1", "req-face", "device-1", "live-1",
                        "IDENTITY", "baseline-ref", "candidate-ref", 88D, "VERIFIED_PENDING",
                        "otp-token", expiresAt, null)));
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.empty());
        when(faceRepository.promote(org.mockito.ArgumentMatchers.eq("face-token"),
                org.mockito.ArgumentMatchers.any())).thenReturn(true);
        doThrow(new ApiException(ApiCode.SERVICE_UNAVAILABLE))
                .when(profileSyncOrchestrator).syncNow(org.mockito.ArgumentMatchers.any());

        assertThatThrownBy(() -> facade.verifyAndChange(
                1L,
                new MobileChangeFacade.MobileChangeVerifyCommand(
                        "req-1", "81222222222", "face-token", "otp-token", "654321", "device-1", DEVICE)))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.apiCode()).isEqualTo(ApiCode.SERVICE_UNAVAILABLE));

        verify(authServiceFacade, never()).openSessionAfterMobileChange(1L, "device-1");
        verify(challengeStore, never()).delete("otp-token");
    }

    @Test
    void rejectsOtpReuseWhenFaceWasAlreadyPromoted() {
        Instant expiresAt = Instant.now().plusSeconds(300);
        when(challengeStore.findByToken("otp-token")).thenReturn(Optional.of(
                new MobileChangeOtpChallenge(
                        "otp-token", 1L, "81222222222", "device-1", "face-token", "654321", expiresAt)));
        when(faceRepository.findVerifiedByToken("face-token")).thenReturn(Optional.of(
                new MobileChangeFaceVerificationRepository.FaceVerificationData(
                        10L, "face-token", 1L, "U1", "req-face", "device-1", "live-1",
                        "IDENTITY", "baseline-ref", "candidate-ref", 88D, "VERIFIED_PENDING",
                        "otp-token", expiresAt, null)));
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.empty());
        when(faceRepository.promote(org.mockito.ArgumentMatchers.eq("face-token"),
                org.mockito.ArgumentMatchers.any())).thenReturn(false);

        assertThatThrownBy(() -> facade.verifyAndChange(
                1L,
                new MobileChangeFacade.MobileChangeVerifyCommand(
                        "req-1", "81222222222", "face-token", "otp-token", "654321", "device-1", DEVICE)))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.apiCode()).isEqualTo(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE));

        verify(authServiceFacade, never()).openSessionAfterMobileChange(1L, "device-1");
        verify(challengeStore, never()).delete("otp-token");
    }

    @Test
    void rejectsCurrentMobileNumberBeforeUpdatingAccount() {
        Instant expiresAt = Instant.now().plusSeconds(300);
        when(challengeStore.findByToken("otp-token")).thenReturn(Optional.of(
                new MobileChangeOtpChallenge(
                        "otp-token", 1L, "81111111111", "device-1", "face-token", "654321", expiresAt)));
        when(faceRepository.findVerifiedByToken("face-token")).thenReturn(Optional.of(
                new MobileChangeFaceVerificationRepository.FaceVerificationData(
                        10L, "face-token", 1L, "U1", "req-face", "device-1", "live-1",
                        "IDENTITY", "baseline-ref", "candidate-ref", 88D, "VERIFIED_PENDING",
                        "otp-token", expiresAt, null)));
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));

        assertThatThrownBy(() -> facade.verifyAndChange(
                1L,
                new MobileChangeFacade.MobileChangeVerifyCommand(
                        "req-1", "81111111111", "face-token", "otp-token", "654321", "device-1", DEVICE)))
                .isInstanceOf(ApiException.class)
                .hasMessage("Silahkan masukkan nomor baru anda");

        verify(userAuthRepository, never()).updateMobileNo(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
        verify(authServiceFacade, never()).openSessionAfterMobileChange(1L, "device-1");
    }

    @Test
    void rejectsSmsDefaultCodeForWhatsAppChallenge() {
        Instant expiresAt = Instant.now().plusSeconds(300);
        when(smsConfigLoader.loadConf()).thenReturn(new SmsConfigLoader.SmsConf(
                false, "url", "spid", "pwd", "0062", "code {code}", 300, 10000,
                "123456", 6, List.of()));
        when(challengeStore.findByToken("otp-token")).thenReturn(Optional.of(
                new MobileChangeOtpChallenge(
                        "otp-token", 1L, "81222222222", "device-1", "face-token", "654321", expiresAt,
                        "WHATSAPP")));
        when(faceRepository.findVerifiedByToken("face-token")).thenReturn(Optional.of(
                new MobileChangeFaceVerificationRepository.FaceVerificationData(
                        10L, "face-token", 1L, "U1", "req-face", "device-1", "live-1",
                        "IDENTITY", "baseline-ref", "candidate-ref", 88D, "VERIFIED_PENDING",
                        "otp-token", expiresAt, null)));
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.empty());
        when(faceRepository.promote(org.mockito.ArgumentMatchers.eq("face-token"),
                org.mockito.ArgumentMatchers.any())).thenReturn(true);

        assertThatThrownBy(() -> facade.verifyAndChange(
                1L,
                new MobileChangeFacade.MobileChangeVerifyCommand(
                        "req-1", "81222222222", "face-token", "otp-token", "123456", "device-1", DEVICE)))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.apiCode()).isEqualTo(ApiCode.INVALID_OR_EXPIRED_VERIFICATION_CODE));

        verify(userAuthRepository, never()).updateMobileNo(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptsWhatsAppDefaultCodeForWhatsAppChallenge() {
        Instant expiresAt = Instant.now().plusSeconds(300);
        when(challengeStore.findByToken("otp-token")).thenReturn(Optional.of(
                new MobileChangeOtpChallenge(
                        "otp-token", 1L, "81222222222", "device-1", "face-token", "654321", expiresAt,
                        "WHATSAPP")));
        when(faceRepository.findVerifiedByToken("face-token")).thenReturn(Optional.of(
                new MobileChangeFaceVerificationRepository.FaceVerificationData(
                        10L, "face-token", 1L, "U1", "req-face", "device-1", "live-1",
                        "IDENTITY", "baseline-ref", "candidate-ref", 88D, "VERIFIED_PENDING",
                        "otp-token", expiresAt, null)));
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.empty());
        when(faceRepository.promote(org.mockito.ArgumentMatchers.eq("face-token"),
                org.mockito.ArgumentMatchers.any())).thenReturn(true);
        TokenPair replacement = TokenPair.of("new-access", "new-refresh", 900);
        when(authServiceFacade.openSessionAfterMobileChange(1L, "device-1")).thenReturn(replacement);

        MobileChangeFacade.MobileChangeResult result = facade.verifyAndChange(
                1L,
                new MobileChangeFacade.MobileChangeVerifyCommand(
                        "req-1", "81222222222", "face-token", "otp-token", "246810", "device-1", DEVICE));

        assertThat(result.tokenPair()).isEqualTo(replacement);
        verify(userAuthRepository).updateMobileNo(1L, "81222222222");
    }
}
