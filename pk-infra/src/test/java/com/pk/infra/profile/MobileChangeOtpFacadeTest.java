package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiException;
import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.MobileChangeOtpChallengeStore;
import com.pk.core.auth.port.SmsSendLogRepository;
import com.pk.core.auth.port.SmsSender;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.infra.auth.AuthOtpConfigLoader;
import com.pk.infra.auth.SmsConfigLoader;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MobileChangeOtpFacadeTest {
    private UserAuthRepository userAuthRepository;
    private MobileChangeFaceVerificationRepository faceRepository;
    private MobileChangeOtpChallengeStore challengeStore;
    private SmsSendLogRepository smsSendLogRepository;
    private SmsSender smsSender;
    private MobileChangeOtpFacade facade;

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        faceRepository = mock(MobileChangeFaceVerificationRepository.class);
        challengeStore = mock(MobileChangeOtpChallengeStore.class);
        smsSendLogRepository = mock(SmsSendLogRepository.class);
        smsSender = mock(SmsSender.class);
        AuthOtpConfigLoader otpConfigLoader = mock(AuthOtpConfigLoader.class);
        SmsConfigLoader smsConfigLoader = mock(SmsConfigLoader.class);
        when(otpConfigLoader.load()).thenReturn(new AuthOtpConfigLoader.AuthOtpConfig(
                10, Duration.ofSeconds(60), ZoneId.of("Asia/Jakarta")));
        when(smsConfigLoader.loadConf()).thenReturn(new SmsConfigLoader.SmsConf(
                true, "url", "spid", "pwd", "0062", "code {code}", 300, 10000,
                "123456", 6, List.of()));
        facade = new MobileChangeOtpFacade(
                userAuthRepository,
                faceRepository,
                challengeStore,
                smsSendLogRepository,
                smsSender,
                otpConfigLoader,
                smsConfigLoader
        );
    }

    @Test
    void sendsPurposeSpecificOtpBoundToVerifiedFace() {
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.empty());
        when(faceRepository.findVerifiedByToken("face-token")).thenReturn(Optional.of(faceVerification()));
        when(smsSendLogRepository.countSince(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(0L);
        when(smsSendLogRepository.insert(org.mockito.ArgumentMatchers.any())).thenReturn(9L);
        when(smsSender.send(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(SmsSendResult.success("OK", "message-1"));

        MobileChangeOtpFacade.OtpSendResult result = facade.send(
                1L,
                new MobileChangeOtpFacade.OtpSendCommand(
                        "req-1", "81222222222", "face-token", "device-1"));

        assertThat(result.otpToken()).isNotBlank();
        verify(smsSendLogRepository).insert(argThat(entry ->
                entry.userId().orElseThrow() == 1L
                        && entry.mobileNo().equals("81222222222")
                        && entry.purpose().equals("MOBILE_CHANGE")));
        verify(challengeStore).save(argThat(challenge ->
                challenge.userId() == 1L
                        && challenge.newMobileNo().equals("81222222222")
                        && challenge.faceVerifyToken().equals("face-token")
                        && challenge.deviceNo().equals("device-1")), org.mockito.ArgumentMatchers.any());
        verify(faceRepository).attachOtpToken("face-token", result.otpToken());
    }

    @Test
    void rejectsCurrentMobileNumberWithNewNumberPrompt() {
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));

        assertThatThrownBy(() -> facade.send(
                1L,
                new MobileChangeOtpFacade.OtpSendCommand(
                        "req-1", "81111111111", "face-token", "device-1")))
                .isInstanceOf(ApiException.class)
                .hasMessage("Silahkan masukkan nomor baru anda");

        verify(smsSender, never()).send(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsAnotherAccountsMobileNumberWithRegisteredPrompt() {
        when(userAuthRepository.findByUserId(1L)).thenReturn(Optional.of(
                new UserProfileSummary(1L, "U1", "81111111111", false)));
        when(userAuthRepository.findActiveByMobileNoExcludingUserId("81222222222", 1L))
                .thenReturn(Optional.of(new UserProfileSummary(2L, "U2", "81222222222", false)));

        assertThatThrownBy(() -> facade.send(
                1L,
                new MobileChangeOtpFacade.OtpSendCommand(
                        "req-1", "81222222222", "face-token", "device-1")))
                .isInstanceOf(ApiException.class)
                .hasMessage("Nomor telepon ini sudah teregister di KTA Kilat. Silahkan masukkan nomor baru anda");

        verify(smsSender, never()).send(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private static MobileChangeFaceVerificationRepository.FaceVerificationData faceVerification() {
        return new MobileChangeFaceVerificationRepository.FaceVerificationData(
                10L, "face-token", 1L, "U1", "req-face", "device-1", "live-1",
                "IDENTITY", "baseline-ref", "candidate-ref", 88D, "VERIFIED_PENDING",
                null, Instant.now().plusSeconds(300), null);
    }
}
