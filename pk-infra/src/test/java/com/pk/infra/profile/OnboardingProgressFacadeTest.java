package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.onboarding.OnboardingModuleCode;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfileIdentityData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.ProfileIdentityRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnboardingProgressFacadeTest {
    private ProfilePersonalRepository profilePersonalRepository;
    private ProfileBankCardRepository profileBankCardRepository;
    private ProfileContactRepository profileContactRepository;
    private ProfileDeviceRepository profileDeviceRepository;
    private ProfileIdentityRepository profileIdentityRepository;
    private OnboardingProgressFacade facade;

    @BeforeEach
    void setUp() {
        profilePersonalRepository = mock(ProfilePersonalRepository.class);
        profileBankCardRepository = mock(ProfileBankCardRepository.class);
        profileContactRepository = mock(ProfileContactRepository.class);
        profileDeviceRepository = mock(ProfileDeviceRepository.class);
        profileIdentityRepository = mock(ProfileIdentityRepository.class);
        facade = new OnboardingProgressFacade(
                profilePersonalRepository,
                profileBankCardRepository,
                profileContactRepository,
                profileDeviceRepository,
                profileIdentityRepository
        );
    }

    @Test
    void returnsIncompleteWhenRequiredModulesMissing() {
        when(profilePersonalRepository.findByProfileId(1L)).thenReturn(Optional.empty());
        when(profileBankCardRepository.findByProfileId(1L)).thenReturn(Optional.empty());
        when(profileContactRepository.findModuleByProfileId(1L)).thenReturn(Optional.empty());
        when(profileDeviceRepository.existsByProfileId(1L)).thenReturn(false);

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_INCOMPLETE);
        assertThat(result.missingModules()).contains(
                OnboardingModuleCode.PERSONAL,
                OnboardingModuleCode.BANK_CARD,
                OnboardingModuleCode.CONTACT,
                OnboardingModuleCode.DEVICE,
                OnboardingModuleCode.IDENTITY
        );
    }

    @Test
    void returnsIncompleteWhenIdentityMissing() {
        stubCompletedModulesExceptIdentity();

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_INCOMPLETE);
        assertThat(result.completedModules()).containsExactlyInAnyOrder(
                OnboardingModuleCode.PERSONAL,
                OnboardingModuleCode.BANK_CARD,
                OnboardingModuleCode.CONTACT,
                OnboardingModuleCode.DEVICE
        );
        assertThat(result.missingModules()).containsExactly(OnboardingModuleCode.IDENTITY);
    }

    @Test
    void returnsSyncedWhenAllRequiredModulesComplete() {
        stubCompletedModulesExceptIdentity();
        EncryptedField encryptedField = new EncryptedField("cipher", new byte[12], new byte[16]);
        when(profileIdentityRepository.findByProfileId(1L)).thenReturn(Optional.of(
                new ProfileIdentityData(1L, "81234567890", "JOHN DOE", encryptedField, "hash", "COMPLETED", "req-1", null, null)
        ));

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_SYNCED);
        assertThat(result.missingModules()).isEmpty();
    }

    private void stubCompletedModulesExceptIdentity() {
        EncryptedField encryptedField = new EncryptedField("cipher", new byte[12], new byte[16]);
        when(profilePersonalRepository.findByProfileId(1L)).thenReturn(Optional.of(
                new ProfilePersonalData(1L, "81234567890", 1, 16, "5000000", encryptedField, null, "COMPLETED", "req-1", null, null)
        ));
        when(profileBankCardRepository.findByProfileId(1L)).thenReturn(Optional.of(
                new ProfileBankCardData(1L, "81234567890", "BCA", encryptedField, "hash", "VERIFIED", null, "COMPLETED", "req-1", null, null)
        ));
        when(profileContactRepository.findModuleByProfileId(1L)).thenReturn(Optional.of(
                new ProfileContactsModuleData(1L, "81234567890", "COMPLETED", "req-1", null, null)
        ));
        when(profileDeviceRepository.existsByProfileId(1L)).thenReturn(true);
        when(profileIdentityRepository.findByProfileId(1L)).thenReturn(Optional.empty());
    }
}
