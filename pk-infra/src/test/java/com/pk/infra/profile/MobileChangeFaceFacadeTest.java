package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.profile.FaceComparisonBaseline;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.infra.ocr.OcrProviderConfigLoader;
import com.pk.infra.ocr.OcrProperties;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MobileChangeFaceFacadeTest {
    private TrustDecisionKycPort trustDecisionKycPort;
    private AdvanceAiOcrPort advanceAiOcrPort;
    private FaceComparisonBaselineResolver baselineResolver;
    private MobileChangeFaceVerificationRepository verificationRepository;
    private BiometricImageStore biometricImageStore;
    private MobileChangeFaceFacade facade;

    @BeforeEach
    void setUp() {
        trustDecisionKycPort = mock(TrustDecisionKycPort.class);
        advanceAiOcrPort = mock(AdvanceAiOcrPort.class);
        baselineResolver = mock(FaceComparisonBaselineResolver.class);
        verificationRepository = mock(MobileChangeFaceVerificationRepository.class);
        biometricImageStore = mock(BiometricImageStore.class);
        OcrProviderConfigLoader configLoader = mock(OcrProviderConfigLoader.class);
        OcrProperties properties = new OcrProperties();
        properties.setFaceThreshold(60);
        when(configLoader.loadAdvanceAi()).thenReturn(properties);
        facade = new MobileChangeFaceFacade(
                trustDecisionKycPort,
                advanceAiOcrPort,
                baselineResolver,
                verificationRepository,
                biometricImageStore,
                configLoader
        );
    }

    @Test
    void verifiesAgainstIdentityFaceAndPersistsPendingCandidate() {
        when(baselineResolver.resolveOrThrow(1L))
                .thenReturn(new FaceComparisonBaseline(FaceComparisonBaseline.TYPE_IDENTITY, "identity-face-ref", null));
        when(biometricImageStore.load("identity-face-ref")).thenReturn(new byte[]{1});
        when(trustDecisionKycPort.retrieveLivenessResult("live-1")).thenReturn(
                new TrustDecisionKycPort.SdkLivenessResult("success", "seq-1", new byte[]{2}, List.of(), 0));
        when(advanceAiOcrPort.compareFaces(any(byte[].class), any(byte[].class)))
                .thenReturn(new AdvanceAiOcrPort.FaceCompareResult(88D));
        when(biometricImageStore.storeVersioned(any(), any(), any(), any()))
                .thenReturn("candidate-face-ref");

        MobileChangeFaceFacade.FaceVerifyResult result = facade.verify(
                1L,
                "U1",
                "81111111111",
                new MobileChangeFaceFacade.FaceVerifyCommand("req-1", "live-1", "device-1", "trace-1")
        );

        assertThat(result.verified()).isTrue();
        assertThat(result.faceVerifyToken()).isNotBlank();
        ArgumentCaptor<MobileChangeFaceVerificationRepository.FaceVerificationInsert> insertCaptor =
                ArgumentCaptor.forClass(MobileChangeFaceVerificationRepository.FaceVerificationInsert.class);
        verify(verificationRepository).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().baselineType()).isEqualTo(FaceComparisonBaseline.TYPE_IDENTITY);
        assertThat(insertCaptor.getValue().candidateFaceEncryptedRef()).isEqualTo("candidate-face-ref");
        assertThat(insertCaptor.getValue().status()).isEqualTo("VERIFIED_PENDING");
    }

}
