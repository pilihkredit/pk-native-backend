package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileWorkRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.reference.BankReferenceFacade;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileServiceFacadeTest {
    private ProfilePersonalRepository profilePersonalRepository;
    private ProfileWorkRepository profileWorkRepository;
    private ProfileContactRepository profileContactRepository;
    private ProfileBankCardRepository profileBankCardRepository;
    private ProfileDeviceRepository profileDeviceRepository;
    private BankReferenceFacade bankReferenceFacade;
    private ProfileSyncOrchestrator profileSyncOrchestrator;
    private ProfileServiceFacade facade;

    @BeforeEach
    void setUp() {
        profilePersonalRepository = mock(ProfilePersonalRepository.class);
        profileWorkRepository = mock(ProfileWorkRepository.class);
        profileContactRepository = mock(ProfileContactRepository.class);
        profileBankCardRepository = mock(ProfileBankCardRepository.class);
        profileDeviceRepository = mock(ProfileDeviceRepository.class);
        bankReferenceFacade = mock(BankReferenceFacade.class);
        profileSyncOrchestrator = mock(ProfileSyncOrchestrator.class);
        SensitiveFieldEncryptor encryptor = new SensitiveFieldEncryptor() {
            @Override
            public EncryptedField encrypt(String plaintext) {
                return new EncryptedField("cipher", new byte[12], new byte[16]);
            }

            @Override
            public String decrypt(EncryptedField encryptedField) {
                return "Siti";
            }
        };
        facade = new ProfileServiceFacade(
                profilePersonalRepository,
                profileWorkRepository,
                profileContactRepository,
                profileBankCardRepository,
                profileDeviceRepository,
                new BasicAreaHierarchyValidator(),
                encryptor,
                new ProfileEnumValidator(new PendanaanProfileEnumCatalog()),
                bankReferenceFacade,
                profileSyncOrchestrator
        );
        when(profilePersonalRepository.findByProfileId(10L)).thenReturn(Optional.empty());
        when(profileWorkRepository.findByProfileId(10L)).thenReturn(Optional.empty());
        when(profileContactRepository.findModuleByProfileId(10L)).thenReturn(Optional.empty());
        when(profileBankCardRepository.findByProfileId(10L)).thenReturn(Optional.empty());
        when(profileBankCardRepository.findByCardNoHash(any())).thenReturn(Optional.empty());
        when(bankReferenceFacade.isValidBankCode("BCA")).thenReturn(true);
    }

    @Test
    void savesPersonalModuleAndReturnsCompleted() {
        var result = facade.savePersonal(10L, "U10001", sampleCommand("req-1"));

        assertThat(result.requestId()).isEqualTo("req-1");
        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profilePersonalRepository).upsert(any());
        verify(profileDeviceRepository).upsert(any());
        verify(profileSyncOrchestrator).scheduleAfterSave(any());
    }

    @Test
    void returnsCompletedWithoutRewriteForSameRequestId() {
        when(profilePersonalRepository.findByProfileId(10L)).thenReturn(Optional.of(
                new ProfilePersonalData(
                        10L,
                        "31",
                        "3171",
                        "317101",
                        "Jl. Example 1",
                        5,
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "user@example.com",
                        "COMPLETED",
                        "req-1"
                )
        ));

        var result = facade.savePersonal(10L, "U10001", sampleCommand("req-1"));

        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profilePersonalRepository, never()).upsert(any());
        verify(profileSyncOrchestrator, never()).scheduleAfterSave(any());
    }

    @Test
    void rejectsInvalidEducationDegree() {
        var command = new ProfileServiceFacade.PersonalSaveCommand(
                "req-2",
                "31",
                "3171",
                "317101",
                "Jl. Example 1",
                9,
                "Siti",
                null,
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.savePersonal(10L, "U10001", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_EDUCATION_DEGREE);
    }

    @Test
    void savesContactsModuleAndReturnsCompleted() {
        var result = facade.saveContacts(10L, "U10001", "81234567890", sampleContactsCommand("req-contact-1"));

        assertThat(result.requestId()).isEqualTo("req-contact-1");
        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profileContactRepository).replaceContacts(anyLong(), any(), any());
        verify(profileDeviceRepository).upsert(any());
        verify(profileSyncOrchestrator).scheduleAfterSave(any());
    }

    @Test
    void returnsCompletedWithoutRewriteForSameContactsRequestId() {
        when(profileContactRepository.findModuleByProfileId(10L)).thenReturn(Optional.of(
                new ProfileContactsModuleData(10L, "COMPLETED", "req-contact-1")
        ));

        var result = facade.saveContacts(10L, "U10001", "81234567890", sampleContactsCommand("req-contact-1"));

        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profileContactRepository, never()).replaceContacts(anyLong(), any(), any());
        verify(profileSyncOrchestrator, never()).scheduleAfterSave(any());
    }

    @Test
    void rejectsContactsBelowMinimumCount() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-2",
                List.of(new ProfileServiceFacade.ContactItemCommand(0, "SITI", "81234567801")),
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "U10001", "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void rejectsInvalidContactRelationship() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-3",
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(99, "SITI", "81234567801"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567802")
                ),
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "U10001", "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_CONTACT_RELATIONSHIP);
    }

    @Test
    void rejectsContactMobileSameAsOwnNumber() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-4",
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(0, "SITI", "81234567890"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567802")
                ),
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "U10001", "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.CONTACT_MOBILE_SAME_AS_OWN);
    }

    @Test
    void rejectsDuplicateContactMobiles() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-5",
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(0, "SITI", "81234567801"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567801")
                ),
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "U10001", "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void rejectsInvalidContactMobileFormat() {
        var command = new ProfileServiceFacade.ContactsSaveCommand(
                "req-contact-6",
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(0, "SITI", "081234567801"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567802")
                ),
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.saveContacts(10L, "U10001", "81234567890", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_MOBILE_NUMBER);
    }

    @Test
    void savesWorkModuleAndReturnsCompleted() {
        var result = facade.saveWork(10L, "U10001", sampleWorkCommand("req-work-1"));

        assertThat(result.requestId()).isEqualTo("req-work-1");
        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        verify(profileWorkRepository).upsert(any());
        verify(profileDeviceRepository).upsert(any());
        verify(profileSyncOrchestrator).scheduleAfterSave(any());
    }

    @Test
    void rejectsInvalidIncomeFormat() {
        var command = new ProfileServiceFacade.WorkSaveCommand(
                "req-work-2",
                1,
                "PT Example",
                "31",
                "3171",
                "317101",
                "Jl. Thamrin",
                "1234",
                25,
                2,
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.saveWork(10L, "U10001", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_INCOME_FORMAT);
    }

    @Test
    void rejectsInvalidPayday() {
        var command = new ProfileServiceFacade.WorkSaveCommand(
                "req-work-3",
                1,
                "PT Example",
                "31",
                "3171",
                "317101",
                "Jl. Thamrin",
                "5000000",
                32,
                2,
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.saveWork(10L, "U10001", command))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_PAYDAY);
    }

    @Test
    void savesBankCardAndReturnsPassedWithMaskedNumber() {
        var result = facade.saveBankCard(10L, "U10001", sampleBankCardCommand("req-bank-1"));

        assertThat(result.requestId()).isEqualTo("req-bank-1");
        assertThat(result.verifyStatus()).isEqualTo("PASSED");
        assertThat(result.cardNoMasked()).isEqualTo("****7890");
        verify(profileSyncOrchestrator).syncNow(any());
        verify(profileDeviceRepository).upsert(any());
        verify(profileBankCardRepository).upsert(any());
    }

    @Test
    void rejectsInvalidBankCode() {
        when(bankReferenceFacade.isValidBankCode("INVALID")).thenReturn(false);

        assertThatThrownBy(() -> facade.saveBankCard(
                10L,
                "U10001",
                new ProfileServiceFacade.BankCardSaveCommand(
                        "req-bank-2",
                        "INVALID",
                        "1234567890",
                        sampleDevice()
                )
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void rejectsBankCardAlreadyBoundToAnotherProfile() {
        when(profileBankCardRepository.findByCardNoHash(any())).thenReturn(Optional.of(
                new com.pk.core.profile.ProfileBankCardData(
                        99L,
                        "BCA",
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "hash",
                        "PASSED",
                        null,
                        "COMPLETED",
                        "req-other"
                )
        ));

        assertThatThrownBy(() -> facade.saveBankCard(10L, "U10001", sampleBankCardCommand("req-bank-3")))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_ALREADY_BOUND);
    }

    private static ProfileServiceFacade.BankCardSaveCommand sampleBankCardCommand(String requestId) {
        return new ProfileServiceFacade.BankCardSaveCommand(requestId, "BCA", "1234567890", sampleDevice());
    }

    private static LenderDeviceContext sampleDevice() {
        return new LenderDeviceContext(
                "PKApp",
                "1.0.0",
                "com.example.pk",
                "device-1",
                "android",
                null,
                null,
                "KEC"
        );
    }

    private static ProfileServiceFacade.ContactsSaveCommand sampleContactsCommand(String requestId) {
        return new ProfileServiceFacade.ContactsSaveCommand(
                requestId,
                List.of(
                        new ProfileServiceFacade.ContactItemCommand(0, "SITI", "81234567801"),
                        new ProfileServiceFacade.ContactItemCommand(1, "AHMAD", "81234567802")
                ),
                sampleDevice()
        );
    }

    private static ProfileServiceFacade.WorkSaveCommand sampleWorkCommand(String requestId) {
        return new ProfileServiceFacade.WorkSaveCommand(
                requestId,
                1,
                "PT Example",
                "31",
                "3171",
                "317101",
                "Jl. Thamrin",
                "5000000",
                25,
                2,
                sampleDevice()
        );
    }

    private static ProfileServiceFacade.PersonalSaveCommand sampleCommand(String requestId) {
        return new ProfileServiceFacade.PersonalSaveCommand(
                requestId,
                "31",
                "3171",
                "317101",
                "Jl. Example 1",
                5,
                "Siti",
                "user@example.com",
                sampleDevice()
        );
    }
}
