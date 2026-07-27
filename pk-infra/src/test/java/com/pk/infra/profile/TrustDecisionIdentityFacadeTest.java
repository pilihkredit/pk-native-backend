package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.ocr.TrustDecisionSessionState;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.TrustDecisionKycPort;
import com.pk.core.profile.port.TrustDecisionSessionStore;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.ocr.TrustDecisionProperties;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrustDecisionIdentityFacadeTest {
    @Test
    void completesIdentityWhenFaceBusinessResultFails() throws Exception {
        TrustDecisionKycPort port = mock(TrustDecisionKycPort.class);
        when(port.compareFaces(any(), any())).thenReturn(
                new TrustDecisionKycPort.FaceCompareResult("fail", 42.5D, "face-seq")
        );
        TrustDecisionSessionStore sessionStore = mock(TrustDecisionSessionStore.class);
        when(sessionStore.find(42L)).thenReturn(Optional.of(session()));
        ProfileIdentityRepository identityRepository = mock(ProfileIdentityRepository.class);
        EncryptedField encrypted = new EncryptedField("cipher", new byte[12], new byte[16]);
        when(identityRepository.findByUserId(42L)).thenReturn(Optional.of(
                new ProfileIdentityData(42L, "TEST USER", encrypted, "hash", "DRAFT", "basic", null)
        ));
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        when(encryptor.decrypt(encrypted)).thenReturn("3201010101010001");
        IdentityVerificationCompletionService completion = mock(IdentityVerificationCompletionService.class);
        when(completion.complete(any())).thenReturn(
                new IdentityVerificationCompletionService.CompletionResult(
                        "COMPLETED", new ObjectMapper().readTree("{\"ok\":true}"))
        );
        TrustDecisionIdentityFacade facade = new TrustDecisionIdentityFacade(
                port,
                sessionStore,
                identityRepository,
                encryptor,
                mock(BiometricImageStore.class),
                completion,
                new TrustDecisionProperties()
        );

        var result = facade.faceRecognition(
                42L,
                "partner-user",
                "81234567890",
                new TrustDecisionIdentityFacade.FaceRecognitionCommand(
                        "request-1",
                        "AQID",
                        "BAUG",
                        new LenderDeviceContext("app", "1.0", "com.pk.app", "device-1", "android")
                ),
                "trace-1"
        );

        assertThat(result.result()).isEqualTo("fail");
        assertThat(result.similarity()).isEqualTo(42.5D);
        verify(completion).complete(any());
    }

    private static TrustDecisionSessionState session() {
        return new TrustDecisionSessionState(
                true,
                true,
                "fail",
                0.41D,
                "live-seq",
                "{\"card_info\":{}}",
                new OcrSessionState.OcrParsedFields(
                        "TEST USER", "3201010101010001", null, null, null, null, null,
                        null, null, null, null, null, null, null, null
                ),
                "encrypted://id-card",
                17L,
                Instant.now()
        );
    }
}
