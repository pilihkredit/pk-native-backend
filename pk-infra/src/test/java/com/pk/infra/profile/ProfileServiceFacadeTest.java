package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.LenderBankCardPort;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileLoginLogRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileTongdunRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.reference.BankReferenceFacade;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileServiceFacadeTest {
    private ProfilePersonalRepository profilePersonalRepository;
    private ProfileContactRepository profileContactRepository;
    private ProfileBankCardRepository profileBankCardRepository;
    private ProfileLoginLogRepository profileLoginLogRepository;
    private ProfileAfRepository profileAfRepository;
    private ProfileTongdunRepository profileTongdunRepository;
    private UserDeviceWriter userDeviceWriter;
    private BankReferenceFacade bankReferenceFacade;
    private ProfileSyncOrchestrator profileSyncOrchestrator;
    private OnboardingProgressFacade onboardingProgressFacade;
    private UserProfileBindingRepository userProfileBindingRepository;
    private ProfileQueryFacade profileQueryFacade;
    private LenderBankCardPort lenderBankCardPort;
    private ProfileServiceFacade facade;

    @BeforeEach
    void setUp() {
        profilePersonalRepository = mock(ProfilePersonalRepository.class);
        profileContactRepository = mock(ProfileContactRepository.class);
        profileBankCardRepository = mock(ProfileBankCardRepository.class);
        profileLoginLogRepository = mock(ProfileLoginLogRepository.class);
        profileAfRepository = mock(ProfileAfRepository.class);
        profileTongdunRepository = mock(ProfileTongdunRepository.class);
        userDeviceWriter = mock(UserDeviceWriter.class);
        bankReferenceFacade = mock(BankReferenceFacade.class);
        profileSyncOrchestrator = mock(ProfileSyncOrchestrator.class);
        onboardingProgressFacade = mock(OnboardingProgressFacade.class);
        userProfileBindingRepository = mock(UserProfileBindingRepository.class);
        profileQueryFacade = mock(ProfileQueryFacade.class);
        lenderBankCardPort = mock(LenderBankCardPort.class);
        BankCardMaxConfigLoader bankCardMaxConfigLoader = mock(BankCardMaxConfigLoader.class);
        when(bankCardMaxConfigLoader.loadMaxCount()).thenReturn(5);
        SensitiveFieldEncryptor encryptor = new SensitiveFieldEncryptor() {
            @Override
            public EncryptedField encrypt(String plaintext) {
                return new EncryptedField("cipher", new byte[12], new byte[16]);
            }

            @Override
            public String decrypt(EncryptedField encryptedField) {
                return "Siti";
            }

            @Override
            public EncryptedField encryptBytes(byte[] plaintext) {
                return encrypt(new String(plaintext));
            }

            @Override
            public byte[] decryptBytes(EncryptedField encryptedField) {
                return decrypt(encryptedField).getBytes();
            }
        };
        facade = new ProfileServiceFacade(
                profilePersonalRepository,
                profileContactRepository,
                profileBankCardRepository,
                profileLoginLogRepository,
                profileAfRepository,
                profileTongdunRepository,
                userDeviceWriter,
                encryptor,
                new ProfileEnumValidator(new PendanaanProfileEnumCatalog()),
                bankReferenceFacade,
                profileSyncOrchestrator,
                onboardingProgressFacade,
                userProfileBindingRepository,
                profileQueryFacade,
                lenderBankCardPort,
                bankCardMaxConfigLoader
        );
        when(onboardingProgressFacade.getProgress(anyLong(), any()))
                .thenReturn(new OnboardingProgressFacade.OnboardingProgressResult(
                        "U10001",
                        OnboardingProgressFacade.KYC_INCOMPLETE,
                        List.of(),
                        List.of("personal")
                ));
        when(profilePersonalRepository.findByProfileId(10L)).thenReturn(Optional.empty());
        when(profileContactRepository.findModuleByProfileId(10L)).thenReturn(Optional.empty());
        when(profileBankCardRepository.findByLastRequestId(any())).thenReturn(Optional.empty());
        when(profileLoginLogRepository.findByProfileId(10L)).thenReturn(Optional.empty());
        when(profileAfRepository.findByRequestId(any())).thenReturn(Optional.empty());
        when(profileTongdunRepository.findByRequestId(any())).thenReturn(Optional.empty());
        when(profileBankCardRepository.findByCardNoHash(any())).thenReturn(Optional.empty());
        when(profileBankCardRepository.countActiveByProfileId(anyLong())).thenReturn(0);
        when(bankReferenceFacade.isValidBankCode("BCA")).thenReturn(true);
        when(profileSyncOrchestrator.scheduleAfterSave(any())).thenReturn(
                new LenderProfileSyncPort.LenderProfileSyncResult(
                        "USR202506020001",
                        "{\"userId\":\"USR202506020001\",\"newUser\":false,\"updatedModules\":[\"profile\",\"device\"]}"
                )
        );
    }

    @Test
    void savesPersonalModuleAndReturnsCompleted() {
        var result = facade.savePersonal(10L, "U10001", "81234567890", sampleCommand("req-1"));

        assertThat(result.requestId()).isEqualTo("req-1");
        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        assertThat(result.lenderResponseJson()).contains("USR202506020001");
        verify(profilePersonalRepository).upsert(any());
        verify(userDeviceWriter).upsertFromRequest(anyLong(), any(), any(), any());
        verify(profileSyncOrchestrator).scheduleAfterSave(any());
    }

    @Test
    void returnsCompletedWithoutRewriteForSameRequestId() {
        when(profilePersonalRepository.findByProfileId(10L)).thenReturn(Optional.of(
                new ProfilePersonalData(
                        10L,
                        "81234567890",
                        5,
                        16,
                        "5000000",
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "user@example.com",
                        "COMPLETED",
                        "req-1",
                        null
                )
        ));

        var result = facade.savePersonal(10L, "U10001", "81234567890", sampleCommand("req-1"));

        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        assertThat(result.lenderResponseJson()).isNull();
        verify(profilePersonalRepository, never()).upsert(any());
        verify(profileSyncOrchestrator, never()).scheduleAfterSave(any());
    }

    @Test
    void rejectsInvalidEducationDegree() {
        var command = new ProfileServiceFacade.PersonalSaveCommand(
                "req-2",
                9,
                16,
                "5000000",
                "Siti",
                null,
                sampleDevice()
        );

        assertThatThrownBy(() -> facade.savePersonal(10L, "U10001", "81234567890", command))
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
        verify(userDeviceWriter).upsertFromRequest(anyLong(), any(), any(), any());
        verify(profileSyncOrchestrator).scheduleAfterSave(any());
    }

    @Test
    void returnsCompletedWithoutRewriteForSameContactsRequestId() {
        when(profileContactRepository.findModuleByProfileId(10L)).thenReturn(Optional.of(
                new ProfileContactsModuleData(10L, "81234567890", "COMPLETED", "req-contact-1", null)
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
    void savesBankCardAndReturnsPassedWithMaskedNumber() {
        var result = facade.saveBankCard(10L, "U10001", "81234567890", sampleBankCardCommand("req-bank-1"));

        assertThat(result.requestId()).isEqualTo("req-bank-1");
        assertThat(result.verifyStatus()).isEqualTo("PASSED");
        assertThat(result.cardNoMasked()).isEqualTo("****7890");
        verify(profileSyncOrchestrator).syncNow(any());
        verify(userDeviceWriter).upsertFromRequest(anyLong(), any(), any(), any());
        verify(profileBankCardRepository).clearDefaultByProfileId(10L);
        verify(profileBankCardRepository).insert(any());
    }

    @Test
    void rejectsBankCardWhenActiveCountReachesMaxWithoutInsert() {
        when(profileBankCardRepository.countActiveByProfileId(10L)).thenReturn(5);

        assertThatThrownBy(() -> facade.saveBankCard(10L, "U10001", "81234567890", sampleBankCardCommand("req-bank-max")))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_MAX_LIMIT_REACHED);
        verify(profileBankCardRepository, never()).insert(any());
        verify(profileBankCardRepository, never()).updateById(any());
        verify(profileSyncOrchestrator, never()).syncNow(any());
    }

    @Test
    void updatesExistingBankCardForSameProfileAndSetsDefault() {
        when(profileBankCardRepository.findByCardNoHash(any())).thenReturn(Optional.of(
                new com.pk.core.profile.ProfileBankCardData(
                        7L,
                        10L,
                        "81234567890",
                        "BCA",
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "hash",
                        "PASSED",
                        null,
                        false,
                        false,
                        "COMPLETED",
                        "req-old",
                        null)
        ));

        var result = facade.saveBankCard(10L, "U10001", "81234567890", sampleBankCardCommand("req-bank-update"));

        assertThat(result.verifyStatus()).isEqualTo("PASSED");
        verify(profileBankCardRepository).clearDefaultByProfileId(10L);
        verify(profileBankCardRepository).updateById(any());
        verify(profileBankCardRepository, never()).insert(any());
        verify(profileSyncOrchestrator).syncNow(any());
    }

    @Test
    void rejectsInvalidBankCode() {
        when(bankReferenceFacade.isValidBankCode("INVALID")).thenReturn(false);

        assertThatThrownBy(() -> facade.saveBankCard(
                10L,
                "U10001",
                "81234567890",
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
                        1L,
                        99L,
                        "81234567890",
                        "BCA",
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "hash",
                        "PASSED",
                        null,
                        true,
                        false,
                        "COMPLETED",
                        "req-other",
                        null
                )
        ));

        assertThatThrownBy(() -> facade.saveBankCard(10L, "U10001", "81234567890", sampleBankCardCommand("req-bank-3")))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_ALREADY_BOUND);
    }

    @Test
    void softDeletesNonDefaultBankCardAfterLenderDelete() throws Exception {
        when(profileBankCardRepository.findActiveByProfileIdAndCardNoHash(anyLong(), any())).thenReturn(Optional.of(
                new com.pk.core.profile.ProfileBankCardData(
                        8L,
                        10L,
                        "81234567890",
                        "BCA",
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "hash",
                        "PASSED",
                        null,
                        false,
                        false,
                        "COMPLETED",
                        "req-old",
                        null
                )
        ));
        ObjectNode root = new ObjectMapper().createObjectNode();
        ObjectNode item = root.putArray("bankCardList").addObject();
        item.put("bankCardId", 10001L);
        item.put("cardNumber", "1234567890");
        when(profileQueryFacade.query(any(), any())).thenReturn(root);

        var result = facade.deleteBankCard(
                10L,
                "U10001",
                "81234567890",
                new ProfileServiceFacade.BankCardDeleteCommand("req-del-1", "1234567890", sampleDevice())
        );

        assertThat(result.deleted()).isTrue();
        verify(lenderBankCardPort).deleteBankCard(any());
        verify(profileBankCardRepository).softDeleteById(8L, "req-del-1");
    }

    @Test
    void rejectsDeletingDefaultBankCard() {
        when(profileBankCardRepository.findActiveByProfileIdAndCardNoHash(anyLong(), any())).thenReturn(Optional.of(
                new com.pk.core.profile.ProfileBankCardData(
                        8L,
                        10L,
                        "81234567890",
                        "BCA",
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "hash",
                        "PASSED",
                        null,
                        true,
                        false,
                        "COMPLETED",
                        "req-old",
                        null
                )
        ));

        assertThatThrownBy(() -> facade.deleteBankCard(
                10L,
                "U10001",
                "81234567890",
                new ProfileServiceFacade.BankCardDeleteCommand("req-del-2", "1234567890", sampleDevice())
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.BANK_CARD_DEFAULT_CANNOT_DELETE);
        verify(lenderBankCardPort, never()).deleteBankCard(any());
    }

    @Test
    void returnsIdempotentSuccessWhenBankCardAlreadySoftDeletedForSameRequestId() {
        when(profileBankCardRepository.findByLastRequestId("req-del-3")).thenReturn(Optional.of(
                new com.pk.core.profile.ProfileBankCardData(
                        8L,
                        10L,
                        "81234567890",
                        "BCA",
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        "hash",
                        "PASSED",
                        null,
                        false,
                        true,
                        "COMPLETED",
                        "req-del-3",
                        null
                )
        ));

        var result = facade.deleteBankCard(
                10L,
                "U10001",
                "81234567890",
                new ProfileServiceFacade.BankCardDeleteCommand("req-del-3", "1234567890", sampleDevice())
        );

        assertThat(result.deleted()).isTrue();
        verify(lenderBankCardPort, never()).deleteBankCard(any());
        verify(profileBankCardRepository, never()).softDeleteById(anyLong(), any());
    }

    @Test
    void savesLoginLogAndReturnsCompletedWithLenderResponse() {
        var result = facade.saveLoginLog(10L, "U10001", "81234567890", sampleLoginLogCommand("req-login-1"));

        assertThat(result.requestId()).isEqualTo("req-login-1");
        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        assertThat(result.lenderResponseJson()).contains("USR202506020001");
        verify(profileLoginLogRepository).upsert(any());
        verify(userDeviceWriter).upsertFromRequest(anyLong(), any(), any(), any());
        verify(profileSyncOrchestrator).scheduleAfterSave(any());
        verify(onboardingProgressFacade, never()).getProgress(anyLong(), any());
    }

    @Test
    void returnsCompletedWithoutRewriteForSameLoginLogRequestId() {
        when(profileLoginLogRepository.findByProfileId(10L)).thenReturn(Optional.of(
                new com.pk.core.profile.ProfileLoginLogData(
                        10L,
                        "81234567890",
                        2,
                        "203.0.113.1",
                        null,
                        null,
                        "COMPLETED",
                        "req-login-1",
                        null
                )
        ));

        var result = facade.saveLoginLog(10L, "U10001", "81234567890", sampleLoginLogCommand("req-login-1"));

        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        assertThat(result.lenderResponseJson()).isNull();
        verify(profileLoginLogRepository, never()).upsert(any());
        verify(profileSyncOrchestrator, never()).scheduleAfterSave(any());
    }

    @Test
    void rejectsInvalidLoginType() {
        assertThatThrownBy(() -> facade.saveLoginLog(
                10L,
                "U10001",
                "81234567890",
                new ProfileServiceFacade.LoginLogSaveCommand(
                        "req-login-2",
                        3,
                        "203.0.113.1",
                        null,
                        null,
                        sampleDevice()
                )
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    private static ProfileServiceFacade.LoginLogSaveCommand sampleLoginLogCommand(String requestId) {
        return new ProfileServiceFacade.LoginLogSaveCommand(
                requestId,
                2,
                "203.0.113.1",
                null,
                null,
                sampleDevice()
        );
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

    private static ProfileServiceFacade.PersonalSaveCommand sampleCommand(String requestId) {
        return new ProfileServiceFacade.PersonalSaveCommand(
                requestId,
                5,
                16,
                "5000000",
                "Siti",
                "user@example.com",
                sampleDevice()
        );
    }
}
