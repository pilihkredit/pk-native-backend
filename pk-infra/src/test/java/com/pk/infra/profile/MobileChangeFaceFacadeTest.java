package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.MobileChangeFaceVerificationRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.infra.ocr.OcrProviderConfigLoader;
import com.pk.infra.ocr.OcrProperties;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MobileChangeFaceFacadeTest {
    private TrustDecisionKycPort trustDecisionKycPort;
    private AdvanceAiOcrPort advanceAiOcrPort;
    private ProfileIdentityRepository profileIdentityRepository;
    private MobileChangeFaceVerificationRepository verificationRepository;
    private BiometricImageStore biometricImageStore;
    private MobileChangeFaceFacade facade;

    @BeforeEach
    void setUp() {
        trustDecisionKycPort = mock(TrustDecisionKycPort.class);
        advanceAiOcrPort = mock(AdvanceAiOcrPort.class);
        profileIdentityRepository = mock(ProfileIdentityRepository.class);
        verificationRepository = mock(MobileChangeFaceVerificationRepository.class);
        biometricImageStore = mock(BiometricImageStore.class);
        OcrProviderConfigLoader configLoader = mock(OcrProviderConfigLoader.class);
        OcrProperties properties = new OcrProperties();
        properties.setFaceThreshold(60);
        when(configLoader.loadAdvanceAi()).thenReturn(properties);
        facade = new MobileChangeFaceFacade(
                trustDecisionKycPort,
                advanceAiOcrPort,
                profileIdentityRepository,
                verificationRepository,
                biometricImageStore,
                configLoader
        );
    }

    @Test
    void verifiesAgainstIdentityFaceAndPersistsPendingCandidate() {
        when(verificationRepository.findLatestPromotedByUserId(1L)).thenReturn(Optional.empty());
        when(profileIdentityRepository.findByUserId(1L)).thenReturn(Optional.of(identity("identity-face-ref")));
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
        assertThat(insertCaptor.getValue().baselineType()).isEqualTo("IDENTITY");
        assertThat(insertCaptor.getValue().candidateFaceEncryptedRef()).isEqualTo("candidate-face-ref");
        assertThat(insertCaptor.getValue().status()).isEqualTo("VERIFIED_PENDING");
    }

    private static ProfileIdentityData identity(String faceRef) {
        return new ProfileIdentityData(
                1L,
                "Test User",
                new EncryptedField("cipher", new byte[]{1}, new byte[]{2}),
                "hash",
                "COMPLETED",
                "req",
                null,
                1L,
                "id-card-ref",
                faceRef,
                "key-ref",
                null,
                null,
                "tongdun",
                null
        );
    }
}
