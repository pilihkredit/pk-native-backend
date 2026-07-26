package com.pk.infra.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.LenderProfileSyncPort;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.ProfileLoginLogRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.SensitiveFieldEncryptor;
import com.pk.core.profile.port.UserProfileBindingRepository;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.ProfileSyncModule;
import com.pk.core.profile.sync.ProfileSyncPayload;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProfileSyncHandlerTest {
    private static final String MOBILE_NO = "81234567890";

    @Test
    void persistsLenderAuditAfterSuccessfulSync() {
        LenderProfileSyncPort lenderProfileSyncPort = mock(LenderProfileSyncPort.class);
        UserProfileBindingRepository userProfileBindingRepository = mock(UserProfileBindingRepository.class);
        ProfilePersonalRepository profilePersonalRepository = mock(ProfilePersonalRepository.class);
        ProfileContactRepository profileContactRepository = mock(ProfileContactRepository.class);
        ProfileBankCardRepository profileBankCardRepository = mock(ProfileBankCardRepository.class);
        ProfileIdentityRepository profileIdentityRepository = mock(ProfileIdentityRepository.class);
        ProfileLoginLogRepository profileLoginLogRepository = mock(ProfileLoginLogRepository.class);
        ProfileSyncPayloadLoader profileSyncPayloadLoader = new ProfileSyncPayloadLoader(
                profilePersonalRepository,
                profileContactRepository,
                mock(SensitiveFieldEncryptor.class)
        );

        when(profilePersonalRepository.findByUserId(7L)).thenReturn(java.util.Optional.of(
                new ProfilePersonalData(
                        7L,
                        5,
                        16,
                        "5000000",
                        new EncryptedField("cipher", new byte[12], new byte[16]),
                        null,
                        "COMPLETED",
                        "REQ-1",
                        null
                )
        ));
        when(lenderProfileSyncPort.syncModule(any()))
                .thenReturn(new LenderProfileSyncPort.LenderProfileSyncResult(
                        "USR202506020001",
                        "{\"userId\":\"USR202506020001\"}",
                        99L
                ));

        ProfileSyncHandler handler = new ProfileSyncHandler(
                lenderProfileSyncPort,
                profileSyncPayloadLoader,
                userProfileBindingRepository,
                profilePersonalRepository,
                profileContactRepository,
                profileBankCardRepository,
                profileIdentityRepository,
                profileLoginLogRepository,
                mock(com.pk.core.profile.port.ProfileAfRepository.class),
                mock(com.pk.core.profile.port.ProfileTongdunRepository.class)
        );

        handler.sync(new ProfileSyncJob(
                7L,
                "UABC",
                MOBILE_NO,
                "REQ-1",
                ProfileSyncModule.PERSONAL,
                sampleDevice(),
                new ProfileSyncPayload.PersonalProfilePayload(5, 16, "5000000", "Siti", null)
        ));

        verify(userProfileBindingRepository).recordLenderProfileSync(7L, "USR202506020001");
        verify(profilePersonalRepository).updateLastLenderInteraction(eq(7L), eq(99L));
    }

    private static LenderDeviceContext sampleDevice() {
        return new LenderDeviceContext(
                "PKApp",
                "1.0.0",
                "com.example.pk",
                "device-1",
                "android",
                "ad-1",
                Map.of(),
                "KEC",
                DeviceExtendedAttributes.empty()
        );
    }
}
