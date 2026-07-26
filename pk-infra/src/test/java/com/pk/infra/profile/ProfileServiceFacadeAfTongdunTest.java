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
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.ProfileTongdunData;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileLoginLogRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileTongdunRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.infra.reference.BankReferenceFacade;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProfileServiceFacadeAfTongdunTest {
    private ProfileAfRepository profileAfRepository;
    private ProfileTongdunRepository profileTongdunRepository;
    private ProfileSyncOrchestrator profileSyncOrchestrator;
    private UserDeviceWriter userDeviceWriter;
    private ProfileServiceFacade facade;

    @BeforeEach
    void setUp() {
        profileAfRepository = mock(ProfileAfRepository.class);
        profileTongdunRepository = mock(ProfileTongdunRepository.class);
        profileSyncOrchestrator = mock(ProfileSyncOrchestrator.class);
        userDeviceWriter = mock(UserDeviceWriter.class);
        OnboardingProgressFacade onboardingProgressFacade = mock(OnboardingProgressFacade.class);
        when(onboardingProgressFacade.getProgress(anyLong(), any()))
                .thenReturn(new OnboardingProgressFacade.OnboardingProgressResult(
                        "U10001",
                        OnboardingProgressFacade.KYC_INCOMPLETE,
                        List.of(),
                        List.of("personal")
                ));
        when(profileAfRepository.findByRequestId(any())).thenReturn(Optional.empty());
        when(profileTongdunRepository.findByRequestId(any())).thenReturn(Optional.empty());
        when(profileSyncOrchestrator.scheduleAfterSave(any())).thenReturn(
                new LenderProfileSyncPort.LenderProfileSyncResult("USR1", "{\"ok\":true}")
        );
        SensitiveFieldEncryptor encryptor = new SensitiveFieldEncryptor() {
            @Override
            public EncryptedField encrypt(String plaintext) {
                return new EncryptedField("c", new byte[12], new byte[16]);
            }

            @Override
            public String decrypt(EncryptedField encryptedField) {
                return "x";
            }

            @Override
            public EncryptedField encryptBytes(byte[] plaintext) {
                return encrypt("x");
            }

            @Override
            public byte[] decryptBytes(EncryptedField encryptedField) {
                return new byte[0];
            }
        };
        facade = new ProfileServiceFacade(
                mock(ProfilePersonalRepository.class),
                mock(ProfileContactRepository.class),
                mock(ProfileBankCardRepository.class),
                mock(ProfileLoginLogRepository.class),
                profileAfRepository,
                profileTongdunRepository,
                userDeviceWriter,
                encryptor,
                new ProfileEnumValidator(new PendanaanProfileEnumCatalog()),
                mock(BankReferenceFacade.class),
                profileSyncOrchestrator,
                onboardingProgressFacade,
                mock(UserProfileBindingRepository.class),
                mock(ProfileQueryFacade.class),
                mock(com.pk.core.profile.port.LenderBankCardPort.class),
                mock(BankCardMaxConfigLoader.class)
        );
    }

    @Test
    void saveAppsFlyerRejectsBlankAppsflyerId() {
        assertThatThrownBy(() -> facade.saveAppsFlyerInstall(
                1L, "U1", "81234567890",
                afCommand("req-1", " ")
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void saveTongdunRejectsInvalidSceneType() {
        assertThatThrownBy(() -> facade.saveTongdunDevice(
                1L, "U1", "81234567890",
                new ProfileServiceFacade.TongdunSaveCommand("req-1", "FOO", "KEY", sampleDevice())
        ))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void saveAppsFlyerIdempotentByRequestId() {
        when(profileAfRepository.findByRequestId("req-1")).thenReturn(Optional.of(
                new ProfileAfData(
                        9L,
                        1L,
                        "device-1",
                        "AF1",
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        "COMPLETED",
                        "req-1",
                        null
                )
        ));

        var result = facade.saveAppsFlyerInstall(1L, "U1", "81234567890", afCommand("req-1", "AF1"));

        assertThat(result.lenderResponseJson()).isNull();
        verify(profileAfRepository, never()).insert(any());
        verify(profileSyncOrchestrator, never()).scheduleAfterSave(any());
    }

    @Test
    void saveAppsFlyerHappyPathStoresOnlyWithoutLenderSync() {
        var result = facade.saveAppsFlyerInstall(1L, "U1", "81234567890", afCommand("req-2", "AF2"));

        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        assertThat(result.lenderResponseJson()).isNull();
        ArgumentCaptor<ProfileAfData> insertCaptor = ArgumentCaptor.forClass(ProfileAfData.class);
        verify(profileAfRepository).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().deviceNo()).isEqualTo("device-1");
        assertThat(insertCaptor.getValue().appsflyerId()).isEqualTo("AF2");
        verify(userDeviceWriter).upsertFromRequest(anyLong(), any(), any(), any());
        verify(profileSyncOrchestrator, never()).scheduleAfterSave(any());
    }

    @Test
    void saveAppsFlyerAnonymousSkipsLenderSync() {
        var result = facade.saveAppsFlyerInstall(null, null, null, afCommand("req-anon", "AF-ANON"));

        assertThat(result.moduleStatus()).isEqualTo("COMPLETED");
        assertThat(result.lenderResponseJson()).isNull();
        ArgumentCaptor<ProfileAfData> insertCaptor = ArgumentCaptor.forClass(ProfileAfData.class);
        verify(profileAfRepository).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().deviceNo()).isEqualTo("device-1");
        verify(userDeviceWriter, never()).upsertFromRequest(anyLong(), any(), any(), any());
        verify(profileSyncOrchestrator, never()).scheduleAfterSave(any());
    }

    @Test
    void saveTongdunHappyPathSchedulesTongdunModule() {
        facade.saveTongdunDevice(
                1L, "U1", "81234567890",
                new ProfileServiceFacade.TongdunSaveCommand("req-3", "LOGIN", "KEY1", sampleDevice())
        );

        verify(profileTongdunRepository).insert(any(ProfileTongdunData.class));
        ArgumentCaptor<ProfileSyncJob> captor = ArgumentCaptor.forClass(ProfileSyncJob.class);
        verify(profileSyncOrchestrator).scheduleAfterSave(captor.capture());
        assertThat(captor.getValue().module()).isEqualTo(ProfileSyncModule.TONGDUN_DEVICE);
    }

    private static ProfileServiceFacade.AppsFlyerSaveCommand afCommand(String requestId, String appsflyerId) {
        return new ProfileServiceFacade.AppsFlyerSaveCommand(
                requestId, appsflyerId, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, sampleDevice()
        );
    }

    private static LenderDeviceContext sampleDevice() {
        return new LenderDeviceContext(
                "PKApp", "1.0.0", "com.example.pk", "device-1", "android", null, null, "KEC"
        );
    }
}
