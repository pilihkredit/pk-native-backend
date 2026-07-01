package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.onboarding.OnboardingModuleCode;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnboardingProgressFacadeTest {
    private ProfilePersonalRepository profilePersonalRepository;
    private ProfileBankCardRepository profileBankCardRepository;
    private ProfileContactRepository profileContactRepository;
    private ProfileDeviceRepository profileDeviceRepository;
    private OnboardingProgressFacade facade;

    @BeforeEach
    void setUp() {
        profilePersonalRepository = mock(ProfilePersonalRepository.class);
        profileBankCardRepository = mock(ProfileBankCardRepository.class);
        profileContactRepository = mock(ProfileContactRepository.class);
        profileDeviceRepository = mock(ProfileDeviceRepository.class);
        facade = new OnboardingProgressFacade(
                profilePersonalRepository,
                profileBankCardRepository,
                profileContactRepository,
                profileDeviceRepository
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
    void returnsSyncedWhenRequiredModulesCompleteEvenIfIdentityMissing() {
        when(profilePersonalRepository.findByProfileId(1L)).thenReturn(Optional.of(mock()));
        when(profileBankCardRepository.findByProfileId(1L)).thenReturn(Optional.of(mock()));
        when(profileContactRepository.findModuleByProfileId(1L)).thenReturn(Optional.of(mock()));
        when(profileDeviceRepository.existsByProfileId(1L)).thenReturn(true);

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_SYNCED);
        assertThat(result.completedModules()).containsExactlyInAnyOrder(
                OnboardingModuleCode.PERSONAL,
                OnboardingModuleCode.BANK_CARD,
                OnboardingModuleCode.CONTACT,
                OnboardingModuleCode.DEVICE
        );
        assertThat(result.missingModules()).containsExactly(OnboardingModuleCode.IDENTITY);
    }
}
