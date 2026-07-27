package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.OcrSessionStore;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.infra.ocr.OcrProperties;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IdentityOcrFacadeBasicSaveTest {
    private ProfileIdentityRepository profileIdentityRepository;
    private SensitiveFieldEncryptor sensitiveFieldEncryptor;
    private IdentityOcrFacade facade;

    @BeforeEach
    void setUp() {
        profileIdentityRepository = mock(ProfileIdentityRepository.class);
        sensitiveFieldEncryptor = mock(SensitiveFieldEncryptor.class);
        OcrProperties ocrProperties = new OcrProperties();
        facade = new IdentityOcrFacade(
                mock(AdvanceAiOcrPort.class),
                mock(OcrSessionStore.class),
                profileIdentityRepository,
                sensitiveFieldEncryptor,
                mock(BiometricImageStore.class),
                mock(ProfileSyncOrchestrator.class),
                mock(com.pk.core.profile.port.ProfileAfRepository.class),
                mock(UserDeviceWriter.class),
                mock(ProfileVersionRepository.class),
                mock(UserProfileBindingRepository.class),
                mock(OnboardingProgressFacade.class),
                mock(IdentityVerificationCompletionService.class),
                ocrProperties,
                new ObjectMapper()
        );
    }

    @Test
    void saveBasicPersistsDraftNameAndEncryptedIdNo() {
        EncryptedField encrypted = new EncryptedField("cipher", new byte[12], new byte[16]);
        when(sensitiveFieldEncryptor.encrypt("3201010101010001")).thenReturn(encrypted);
        when(profileIdentityRepository.findByUserId(1L)).thenReturn(Optional.empty());

        var result = facade.saveBasic(
                1L,
                "81234567890",
                new IdentityOcrFacade.BasicSaveCommand(
                        "req-1",
                        "JOHN DOE",
                        "3201010101010001"
                )
        );

        assertThat(result.moduleStatus()).isEqualTo(IdentityOcrFacade.MODULE_DRAFT);
        ArgumentCaptor<ProfileIdentityData> captor = ArgumentCaptor.forClass(ProfileIdentityData.class);
        verify(profileIdentityRepository).upsert(captor.capture());
        assertThat(captor.getValue().fullName()).isEqualTo("JOHN DOE");
        assertThat(captor.getValue().idNo()).isEqualTo(encrypted);
        assertThat(captor.getValue().moduleStatus()).isEqualTo(IdentityOcrFacade.MODULE_DRAFT);
    }

    @Test
    void saveBasicRejectsWhenIdentityAlreadyCompleted() {
        when(profileIdentityRepository.findByUserId(1L)).thenReturn(Optional.of(
                new ProfileIdentityData(
                        1L,
                        "OLD",
                        new EncryptedField("c", new byte[12], new byte[16]),
                        "hash",
                        IdentityOcrFacade.MODULE_COMPLETED,
                        "old-req",
                        null
                )
        ));

        assertThatThrownBy(() -> facade.saveBasic(
                1L,
                "81234567890",
                new IdentityOcrFacade.BasicSaveCommand(
                        "req-2",
                        "JOHN DOE",
                        "3201010101010001"
                )
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.DUPLICATE_SUBMISSION_IN_PROGRESS);
    }

    @Test
    void saveBasicRejectsInvalidEktp() {
        assertThatThrownBy(() -> facade.saveBasic(
                1L,
                "81234567890",
                new IdentityOcrFacade.BasicSaveCommand(
                        "req-1",
                        "JOHN DOE",
                        "123"
                )
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_EKTP_FORMAT);
    }
}
