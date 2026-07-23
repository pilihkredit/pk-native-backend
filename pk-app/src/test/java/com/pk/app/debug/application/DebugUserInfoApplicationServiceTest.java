package com.pk.app.debug.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.port.BiometricImageStore;
import com.pk.core.profile.port.OcrSessionStore;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.infra.debug.mapper.DebugUserInfoReadMapper;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DebugUserInfoApplicationServiceTest {
    @Test
    void returnsDecryptedProfileFieldsAndImages() {
        UserAuthRepository userAuthRepository = mock(UserAuthRepository.class);
        OnboardingProgressFacade onboardingProgressFacade = mock(OnboardingProgressFacade.class);
        ProfileIdentityRepository profileIdentityRepository = mock(ProfileIdentityRepository.class);
        ProfilePersonalRepository profilePersonalRepository = mock(ProfilePersonalRepository.class);
        ProfileContactRepository profileContactRepository = mock(ProfileContactRepository.class);
        ProfileBankCardRepository profileBankCardRepository = mock(ProfileBankCardRepository.class);
        SensitiveFieldEncryptor encryptor = mock(SensitiveFieldEncryptor.class);
        BiometricImageStore biometricImageStore = mock(BiometricImageStore.class);
        OcrSessionStore ocrSessionStore = mock(OcrSessionStore.class);
        ObjectMapper objectMapper = new ObjectMapper();
        DebugUserInfoReadMapper readMapper = mock(DebugUserInfoReadMapper.class);
        DebugUserProgressProperties properties = new DebugUserProgressProperties();
        properties.setEnabled(true);
        properties.setToken("debug-token");

        DebugUserInfoApplicationService service = new DebugUserInfoApplicationService(
                userAuthRepository,
                onboardingProgressFacade,
                profileIdentityRepository,
                profilePersonalRepository,
                profileContactRepository,
                profileBankCardRepository,
                encryptor,
                biometricImageStore,
                ocrSessionStore,
                objectMapper,
                readMapper,
                properties
        );

        EncryptedField idNo = new EncryptedField("id-cipher", new byte[12], new byte[16]);
        EncryptedField motherSurname = new EncryptedField("mother-cipher", new byte[12], new byte[16]);
        EncryptedField cardNo = new EncryptedField("card-cipher", new byte[12], new byte[16]);

        when(userAuthRepository.findByMobileNo("801234567")).thenReturn(Optional.of(
                new UserProfileSummary(10L, "U10001", "801234567", false)
        ));
        when(readMapper.findAccountByProfileId(10L)).thenReturn(new DebugUserInfoReadMapper.UserAccountRecord(
                10L,
                "U10001",
                "EXT10001",
                "801234567",
                "user@example.com",
                null,
                "PENDING",
                Instant.parse("2026-07-01T00:00:00Z"),
                Instant.parse("2026-07-01T00:00:00Z"),
                Instant.parse("2026-07-02T00:00:00Z")
        ));
        when(onboardingProgressFacade.getProgress(10L, "U10001")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "U10001",
                        "INCOMPLETE",
                        List.of("PERSONAL"),
                        List.of("IDENTITY")
                )
        );
        when(readMapper.findLatestIdentityAssetByProfileId(10L)).thenReturn(
                new DebugUserInfoReadMapper.IdentityAssetRecord(
                        1L,
                        "DAVID HARTANTO",
                        "id-card-ref",
                        "face-ref",
                        "ADVANCE_AI",
                        "{\"gender\":\"MALE\",\"address\":\"Jakarta\"}",
                        Instant.parse("2026-07-01T01:00:00Z")
                )
        );
        when(readMapper.findDevicesByProfileId(10L)).thenReturn(List.of(
                new DebugUserInfoReadMapper.DeviceRecord(
                        "device-1",
                        "ANDROID",
                        "KTA",
                        "1.0.0",
                        "com.pk.app",
                        "Pixel",
                        "Pixel 8",
                        null,
                        "14",
                        null,
                        8,
                        8L,
                        128L,
                        "ad-1",
                        null,
                        null,
                        null,
                        "{\"model\":\"Pixel\"}",
                        "req-device-1",
                        Instant.parse("2026-07-01T02:00:00Z")
                )
        ));
        when(profileIdentityRepository.findByProfileId(10L)).thenReturn(Optional.of(
                new ProfileIdentityData(
                        10L,
                        "801234567",
                        "DAVID HARTANTO",
                        idNo,
                        "hash-id",
                        "COMPLETED",
                        "req-id-1",
                        null,
                        null
                )
        ));
        when(profilePersonalRepository.findByProfileId(10L)).thenReturn(Optional.of(
                new ProfilePersonalData(
                        10L,
                        "801234567",
                        5,
                        16,
                        "5000000",
                        motherSurname,
                        "personal@example.com",
                        "COMPLETED",
                        "req-personal-1",
                        null,
                        null
                )
        ));
        when(profileContactRepository.findModuleByProfileId(10L)).thenReturn(Optional.of(
                new ProfileContactsModuleData(10L, "801234567", "COMPLETED", "req-contact-1", null, null)
        ));
        when(profileContactRepository.findContactsByProfileId(10L)).thenReturn(List.of(
                new ProfileContactData("801234567", 0, 1, "SITI", "81234567801")
        ));
        when(profileBankCardRepository.findDefaultByProfileId(10L)).thenReturn(Optional.of(
                new ProfileBankCardData(
                        1L,
                        10L,
                        "801234567",
                        "OCBC",
                        cardNo,
                        "hash-card",
                        "FAILED",
                        "A000322",
                        true,
                        "COMPLETED",
                        "req-bank-1",
                        null,
                        null
                )
        ));
        when(ocrSessionStore.find(10L)).thenReturn(Optional.of(
                new OcrSessionState(
                        true,
                        true,
                        true,
                        98,
                        "{\"raw\":\"ocr\"}",
                        new OcrSessionState.OcrParsedFields(
                                "DAVID HARTANTO",
                                "3301234567890001",
                                "MALE",
                                "ISLAM",
                                "SINGLE",
                                "1990-01-01",
                                "JAKARTA",
                                "Jakarta Selatan",
                                "ENGINEER",
                                "ID",
                                "O",
                                "2030-01-01",
                                "DKI",
                                "Jakarta",
                                "Kebayoran"
                        ),
                        "id-card-ref",
                        Instant.parse("2026-07-01T01:30:00Z")
                )
        ));
        when(encryptor.decrypt(idNo)).thenReturn("3301234567890001");
        when(encryptor.decrypt(motherSurname)).thenReturn("SITI");
        when(encryptor.decrypt(cardNo)).thenReturn("1234567890");
        when(biometricImageStore.load(anyString())).thenReturn(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});

        var response = service.query("debug-token", "801234567");

        assertThat(response.found()).isTrue();
        assertThat(response.account().partnerUserId()).isEqualTo("U10001");
        assertThat(response.identity().idNo()).isEqualTo("3301234567890001");
        assertThat(response.identity().gender()).isEqualTo("MALE");
        assertThat(response.identity().address()).isEqualTo("Jakarta Selatan");
        assertThat(response.identity().idCardImageDataUrl()).startsWith("data:image/jpeg;base64,");
        assertThat(response.identity().facePhotoDataUrl()).startsWith("data:image/jpeg;base64,");
        assertThat(response.personal().motherSurname()).isEqualTo("SITI");
        assertThat(response.bankCard().cardNumber()).isEqualTo("1234567890");
        assertThat(response.contacts().items()).hasSize(1);
        assertThat(response.devices()).hasSize(1);
        assertThat(response.ocrSession().livenessScore()).isEqualTo(98);
    }

    @Test
    void returnsNotFoundWhenUserMissing() {
        UserAuthRepository userAuthRepository = mock(UserAuthRepository.class);
        DebugUserProgressProperties properties = new DebugUserProgressProperties();
        properties.setEnabled(true);
        properties.setToken("debug-token");
        when(userAuthRepository.findByMobileNo("899999999")).thenReturn(Optional.empty());

        DebugUserInfoApplicationService service = new DebugUserInfoApplicationService(
                userAuthRepository,
                mock(OnboardingProgressFacade.class),
                mock(ProfileIdentityRepository.class),
                mock(ProfilePersonalRepository.class),
                mock(ProfileContactRepository.class),
                mock(ProfileBankCardRepository.class),
                mock(SensitiveFieldEncryptor.class),
                mock(BiometricImageStore.class),
                mock(OcrSessionStore.class),
                new ObjectMapper(),
                mock(DebugUserInfoReadMapper.class),
                properties
        );

        assertThat(service.query("debug-token", "899999999").found()).isFalse();
    }
}
