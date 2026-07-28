package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IdentityVerificationCompletionServiceTest {
    @Test
    void persistsProviderChannelAndCallsLender() {
        ProfileIdentityRepository identityRepository = mock(ProfileIdentityRepository.class);
        BiometricImageStore imageStore = mock(BiometricImageStore.class);
        when(imageStore.store(any(), any(), any())).thenReturn("encrypted://image");
        when(imageStore.encryptionKeyRef()).thenReturn("key-ref");
        ProfileVersionRepository versionRepository = mock(ProfileVersionRepository.class);
        when(versionRepository.createSnapshot(any(Long.class), any(), any(), any())).thenReturn(9L);
        ProfileSyncOrchestrator orchestrator = mock(ProfileSyncOrchestrator.class);
        when(orchestrator.scheduleAfterSave(any())).thenReturn(
                new LenderProfileSyncPort.LenderProfileSyncResult("external", "{\"ok\":true}")
        );
        OnboardingProgressFacade progressFacade = mock(OnboardingProgressFacade.class);
        when(progressFacade.getProgress(42L, "partner-user")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-user", "COMPLETED", List.of("identity"), List.of()
                )
        );
        UserProfileBindingRepository bindingRepository = mock(UserProfileBindingRepository.class);
        IdentityVerificationCompletionService service = new IdentityVerificationCompletionService(
                identityRepository,
                imageStore,
                orchestrator,
                mock(ProfileAfRepository.class),
                mock(AppsFlyerLenderPayloadResolver.class),
                mock(UserDeviceWriter.class),
                versionRepository,
                bindingRepository,
                progressFacade,
                new ObjectMapper()
        );
        EncryptedField encryptedId = new EncryptedField("cipher", new byte[12], new byte[16]);

        var result = service.complete(new IdentityVerificationCompletionCommand(
                42L,
                "partner-user",
                "81234567890",
                "request-1",
                "TEST USER",
                "3201010101010001",
                encryptedId,
                "id-hash",
                "trustDecision",
                17L,
                parsed(),
                "{\"card_info\":{}}",
                new byte[] {1},
                new byte[] {2},
                "liveness-sequence",
                mock(LenderDeviceContext.class)
        ));

        ArgumentCaptor<ProfileIdentityData> captor = ArgumentCaptor.forClass(ProfileIdentityData.class);
        verify(identityRepository).upsert(captor.capture());
        assertThat(captor.getValue().ocrChannel()).isEqualTo("trustDecision");
        assertThat(captor.getValue().ocrVendorCallLogId()).isEqualTo(17L);
        verify(orchestrator).scheduleAfterSave(any());
        assertThat(result.lenderResponse().path("ok").asBoolean()).isTrue();
    }

    private static OcrSessionState.OcrParsedFields parsed() {
        return new OcrSessionState.OcrParsedFields(
                "TEST USER", "3201010101010001", null, null, null, null, null,
                null, null, null, null, null, null, null, null
        );
    }
}
