package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TrustDecisionIdentityFacadeTest {
    @Test
    void obtainsSdkLivenessLicenseFromProvider() {
        TrustDecisionKycPort port = mock(TrustDecisionKycPort.class);
        when(port.obtainLivenessLicense(600)).thenReturn(
                new TrustDecisionKycPort.LivenessLicense("license-value", 1785147934L, "license-seq"));
        TrustDecisionIdentityFacade facade = new TrustDecisionIdentityFacade(
                port,
                mock(TrustDecisionSessionStore.class),
                mock(ProfileIdentityRepository.class),
                mock(SensitiveFieldEncryptor.class),
                mock(BiometricImageStore.class),
                mock(IdentityVerificationCompletionService.class),
                new TrustDecisionProperties());

        var result = facade.obtainLivenessLicense(
                42L, "partner-user", "81234567890", 600, "request-1", "trace-1");

        assertThat(result.license()).isEqualTo("license-value");
        assertThat(result.expiryTimestamp()).isEqualTo(1785147934L);
        assertThat(result.sequenceId()).isEqualTo("license-seq");
    }

    @Test
    void completesInteractiveLivenessWithSdkLivenessId() throws Exception {
        TrustDecisionKycPort port = mock(TrustDecisionKycPort.class);
        when(port.retrieveLivenessResult("sdk-live-id")).thenReturn(
                new TrustDecisionKycPort.SdkLivenessResult(
                        "pass", "result-seq", new byte[] {7, 8}, List.of(), 0));
        TrustDecisionSessionStore sessionStore = mock(TrustDecisionSessionStore.class);
        when(sessionStore.find(42L)).thenReturn(Optional.of(session()));
        ProfileIdentityRepository identityRepository = mock(ProfileIdentityRepository.class);
        EncryptedField encrypted = new EncryptedField("cipher", new byte[12], new byte[16]);
        when(identityRepository.findByUserId(42L)).thenReturn(Optional.of(
                new ProfileIdentityData(42L, "TEST USER", encrypted, "hash", "DRAFT", "basic", null)));
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        when(encryptor.decrypt(encrypted)).thenReturn("3201010101010001");
        BiometricImageStore imageStore = mock(BiometricImageStore.class);
        when(imageStore.load("encrypted://id-card")).thenReturn(new byte[] {1, 2});
        IdentityVerificationCompletionService completion = mock(IdentityVerificationCompletionService.class);
        when(completion.complete(any())).thenReturn(
                new IdentityVerificationCompletionService.CompletionResult(
                        "COMPLETED", new ObjectMapper().readTree("{\"ok\":true}")));
        TrustDecisionIdentityFacade facade = new TrustDecisionIdentityFacade(
                port, sessionStore, identityRepository, encryptor, imageStore, completion,
                new TrustDecisionProperties());

        var result = facade.completeInteractiveLiveness(
                42L, "partner-user", "81234567890", "request-2", "sdk-live-id",
                new LenderDeviceContext("app", "1.0", "com.pk.app", "device-1", "android"), "trace-1");

        ArgumentCaptor<IdentityVerificationCompletionCommand> commandCaptor =
                ArgumentCaptor.forClass(IdentityVerificationCompletionCommand.class);
        verify(completion).complete(commandCaptor.capture());
        assertThat(commandCaptor.getValue().livenessId()).isEqualTo("sdk-live-id");
        assertThat(commandCaptor.getValue().faceImage()).containsExactly(7, 8);
        assertThat(result.sequenceId()).isEqualTo("result-seq");
        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void stopsOcrBusinessFailureEvenWhenVendorReturnsCardFields() {
        TrustDecisionKycPort port = mock(TrustDecisionKycPort.class);
        var parsed = new OcrSessionState.OcrParsedFields(
                "TEST USER", "3201010101010001", null, null, null, null, null,
                null, null, null, null, null, null, null, null);
        when(port.checkIdentityCard(any())).thenReturn(
                new TrustDecisionKycPort.OcrResult("fail", "ocr-seq", "{}", parsed));
        TrustDecisionSessionStore sessionStore = mock(TrustDecisionSessionStore.class);
        BiometricImageStore imageStore = mock(BiometricImageStore.class);
        TrustDecisionIdentityFacade facade = new TrustDecisionIdentityFacade(
                port,
                sessionStore,
                mock(ProfileIdentityRepository.class),
                mock(SensitiveFieldEncryptor.class),
                imageStore,
                mock(IdentityVerificationCompletionService.class),
                new TrustDecisionProperties());

        assertThatThrownBy(() -> facade.ocrCheck(
                42L, "partner-user", "81234567890", "AQID", "request-1", "trace-1"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.apiCode()).isEqualTo(ApiCode.OCR_NO_RESULT));

        verify(sessionStore, never()).save(any(Long.class), any());
        verify(imageStore, never()).store(any(), any(), any());
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
