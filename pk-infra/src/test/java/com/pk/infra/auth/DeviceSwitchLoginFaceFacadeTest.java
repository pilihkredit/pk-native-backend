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
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.DeviceSwitchFaceVerificationRepository;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.FaceComparisonBaseline;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.infra.ocr.OcrProviderConfigLoader;
import com.pk.infra.ocr.OcrProperties;
import com.pk.infra.profile.FaceComparisonBaselineResolver;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DeviceSwitchLoginFaceFacadeTest {
    private UserAuthRepository userAuthRepository;
    private TrustDecisionKycPort trustDecisionKycPort;
    private AdvanceAiOcrPort advanceAiOcrPort;
    private FaceComparisonBaselineResolver baselineResolver;
    private DeviceSwitchFaceVerificationRepository verificationRepository;
    private BiometricImageStore biometricImageStore;
    private DeviceSwitchLoginFaceAttemptLimiter attemptLimiter;
    private DeviceSwitchLoginFaceFacade facade;

    @BeforeEach
    void setUp() {
        userAuthRepository = mock(UserAuthRepository.class);
        trustDecisionKycPort = mock(TrustDecisionKycPort.class);
        advanceAiOcrPort = mock(AdvanceAiOcrPort.class);
        baselineResolver = mock(FaceComparisonBaselineResolver.class);
        verificationRepository = mock(DeviceSwitchFaceVerificationRepository.class);
        biometricImageStore = mock(BiometricImageStore.class);
        attemptLimiter = mock(DeviceSwitchLoginFaceAttemptLimiter.class);
        OcrProviderConfigLoader configLoader = mock(OcrProviderConfigLoader.class);
        OcrProperties properties = new OcrProperties();
        properties.setFaceThreshold(60);
        when(configLoader.loadAdvanceAi()).thenReturn(properties);
        facade = new DeviceSwitchLoginFaceFacade(
                userAuthRepository,
                trustDecisionKycPort,
                advanceAiOcrPort,
                baselineResolver,
                verificationRepository,
                biometricImageStore,
                configLoader,
                attemptLimiter
        );
    }

    @Test
    void rejectsInvalidMobileNumber() {
        assertThatThrownBy(() -> facade.verify(
                "+8613812345678",
                new DeviceSwitchLoginFaceFacade.FaceVerifyCommand("req", "live", "device-1")
        ))
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_MOBILE_NUMBER);
    }

    @Test
    void rejectsUnknownMobileWithoutEnumeratingRegistration() {
        when(userAuthRepository.findByMobileNo("8123456789")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.verify(
                "8123456789",
                new DeviceSwitchLoginFaceFacade.FaceVerifyCommand("req", "live", "device-1")
        ))
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void reusesPendingTicketForSameUserAndRequestIdWithoutCallingVendors() {
        UserProfileSummary profile = new UserProfileSummary(1L, "U1", "8123456789", false);
        when(userAuthRepository.findByMobileNo("8123456789")).thenReturn(Optional.of(profile));
        Instant expiresAt = Instant.now().plusSeconds(240);
        when(verificationRepository.findReusablePendingByUserIdAndRequestId(1L, "req-1"))
                .thenReturn(Optional.of(new DeviceSwitchFaceVerificationRepository.FaceVerificationData(
                        10L,
                        "existing-token",
                        1L,
                        "U1",
                        "req-1",
                        "device-1",
                        "live-1",
                        "IDENTITY",
                        90D,
                        "VERIFIED_PENDING",
                        expiresAt,
                        null
                )));

        DeviceSwitchLoginFaceFacade.FaceVerifyResult result = facade.verify(
                "8123456789",
                new DeviceSwitchLoginFaceFacade.FaceVerifyCommand("req-1", "live-1", "device-1")
        );

        assertThat(result.faceVerifyToken()).isEqualTo("existing-token");
        assertThat(result.expiresIn()).isBetween(230L, 240L);
        verify(trustDecisionKycPort, never()).retrieveLivenessResult(any());
        verify(verificationRepository, never()).insert(any());
    }

    @Test
    void verifiesAndPersistsPendingTicketOnSuccess() {
        UserProfileSummary profile = new UserProfileSummary(1L, "U1", "8123456789", false);
        when(userAuthRepository.findByMobileNo("8123456789")).thenReturn(Optional.of(profile));
        when(verificationRepository.findReusablePendingByUserIdAndRequestId(1L, "req-1"))
                .thenReturn(Optional.empty());
        when(baselineResolver.resolveOrThrow(1L))
                .thenReturn(new FaceComparisonBaseline(FaceComparisonBaseline.TYPE_IDENTITY, "baseline-ref", null));
        when(biometricImageStore.load("baseline-ref")).thenReturn(new byte[]{1});
        when(trustDecisionKycPort.retrieveLivenessResult("live-1")).thenReturn(
                new TrustDecisionKycPort.SdkLivenessResult("success", "seq-1", new byte[]{2}, List.of(), 0));
        when(advanceAiOcrPort.compareFaces(any(byte[].class), any(byte[].class)))
                .thenReturn(new AdvanceAiOcrPort.FaceCompareResult(88D));
        when(biometricImageStore.storeVersioned(any(), any(), any(), any())).thenReturn("candidate-ref");

        DeviceSwitchLoginFaceFacade.FaceVerifyResult result = facade.verify(
                "8123456789",
                new DeviceSwitchLoginFaceFacade.FaceVerifyCommand("req-1", "live-1", "  device-1  ")
        );

        assertThat(result.verified()).isTrue();
        assertThat(result.faceVerifyToken()).isNotBlank();
        assertThat(result.expiresIn()).isBetween(290L, 300L);
        ArgumentCaptor<DeviceSwitchFaceVerificationRepository.FaceVerificationInsert> captor =
                ArgumentCaptor.forClass(DeviceSwitchFaceVerificationRepository.FaceVerificationInsert.class);
        verify(verificationRepository).insert(captor.capture());
        assertThat(captor.getValue().deviceNo()).isEqualTo("device-1");
        assertThat(captor.getValue().status()).isEqualTo("VERIFIED_PENDING");
        verify(attemptLimiter).clearFailures("8123456789", "device-1");
    }

    @Test
    void recordsFailureAndRejectsWhenLivenessFails() {
        UserProfileSummary profile = new UserProfileSummary(1L, "U1", "8123456789", false);
        when(userAuthRepository.findByMobileNo("8123456789")).thenReturn(Optional.of(profile));
        when(verificationRepository.findReusablePendingByUserIdAndRequestId(1L, "req-1"))
                .thenReturn(Optional.empty());
        when(baselineResolver.resolveOrThrow(1L))
                .thenReturn(new FaceComparisonBaseline(FaceComparisonBaseline.TYPE_IDENTITY, "baseline-ref", null));
        when(biometricImageStore.load("baseline-ref")).thenReturn(new byte[]{1});
        when(trustDecisionKycPort.retrieveLivenessResult("live-1")).thenReturn(
                new TrustDecisionKycPort.SdkLivenessResult("fail", "seq-1", new byte[0], List.of(), 0));

        assertThatThrownBy(() -> facade.verify(
                "8123456789",
                new DeviceSwitchLoginFaceFacade.FaceVerifyCommand("req-1", "live-1", "device-1")
        ))
                .extracting("apiCode")
                .isEqualTo(ApiCode.OCR_LIVENESS_FAILED);

        verify(attemptLimiter).recordFailure("8123456789", "device-1");
        verify(verificationRepository, never()).insert(any());
    }

    @Test
    void recordsFailureWhenCompareBelowThreshold() {
        UserProfileSummary profile = new UserProfileSummary(1L, "U1", "8123456789", false);
        when(userAuthRepository.findByMobileNo("8123456789")).thenReturn(Optional.of(profile));
        when(verificationRepository.findReusablePendingByUserIdAndRequestId(1L, "req-1"))
                .thenReturn(Optional.empty());
        when(baselineResolver.resolveOrThrow(1L))
                .thenReturn(new FaceComparisonBaseline(FaceComparisonBaseline.TYPE_IDENTITY, "baseline-ref", null));
        when(biometricImageStore.load("baseline-ref")).thenReturn(new byte[]{1});
        when(trustDecisionKycPort.retrieveLivenessResult("live-1")).thenReturn(
                new TrustDecisionKycPort.SdkLivenessResult("success", "seq-1", new byte[]{2}, List.of(), 0));
        when(advanceAiOcrPort.compareFaces(any(byte[].class), any(byte[].class)))
                .thenReturn(new AdvanceAiOcrPort.FaceCompareResult(40D));
        when(biometricImageStore.storeVersioned(any(), any(), any(), any())).thenReturn("candidate-ref");

        assertThatThrownBy(() -> facade.verify(
                "8123456789",
                new DeviceSwitchLoginFaceFacade.FaceVerifyCommand("req-1", "live-1", "device-1")
        ))
                .extracting("apiCode")
                .isEqualTo(ApiCode.FACE_RECOGNITION_FAILED);

        ArgumentCaptor<DeviceSwitchFaceVerificationRepository.FaceVerificationInsert> captor =
                ArgumentCaptor.forClass(DeviceSwitchFaceVerificationRepository.FaceVerificationInsert.class);
        verify(verificationRepository).insert(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo("FAILED");
        verify(attemptLimiter).recordFailure(eq("8123456789"), eq("device-1"));
    }

    @Test
    void propagatesRateLimitFromAttemptLimiter() {
        UserProfileSummary profile = new UserProfileSummary(1L, "U1", "8123456789", false);
        when(userAuthRepository.findByMobileNo("8123456789")).thenReturn(Optional.of(profile));
        org.mockito.Mockito.doThrow(new ApiException(ApiCode.TOO_MANY_REQUESTS))
                .when(attemptLimiter)
                .assertAllowed("8123456789", "device-1");

        assertThatThrownBy(() -> facade.verify(
                "8123456789",
                new DeviceSwitchLoginFaceFacade.FaceVerifyCommand("req-1", "live-1", "device-1")
        ))
                .extracting("apiCode")
                .isEqualTo(ApiCode.TOO_MANY_REQUESTS);

        verify(verificationRepository, never()).insert(any());
    }
}
