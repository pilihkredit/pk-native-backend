package com.pk.infra.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.onboarding.OnboardingModuleCode;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileWorkRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnboardingProgressFacadeTest {
    private ProfilePersonalRepository profilePersonalRepository;
    private ProfileWorkRepository profileWorkRepository;
    private ProfileBankCardRepository profileBankCardRepository;
    private ProfileContactRepository profileContactRepository;
    private ProfileDeviceRepository profileDeviceRepository;
    private OnboardingProgressFacade facade;

    @BeforeEach
    void setUp() {
        profilePersonalRepository = mock(ProfilePersonalRepository.class);
        profileWorkRepository = mock(ProfileWorkRepository.class);
        profileBankCardRepository = mock(ProfileBankCardRepository.class);
        profileContactRepository = mock(ProfileContactRepository.class);
        profileDeviceRepository = mock(ProfileDeviceRepository.class);
        facade = new OnboardingProgressFacade(
                profilePersonalRepository,
                profileWorkRepository,
                profileBankCardRepository,
                profileContactRepository,
                profileDeviceRepository
        );
    }

    @Test
    void returnsIncompleteWhenRequiredModulesMissing() {
        when(profilePersonalRepository.findByProfileId(1L)).thenReturn(Optional.empty());
        when(profileWorkRepository.findByProfileId(1L)).thenReturn(Optional.empty());
        when(profileBankCardRepository.findByProfileId(1L)).thenReturn(Optional.empty());
        when(profileContactRepository.findModuleByProfileId(1L)).thenReturn(Optional.empty());
        when(profileDeviceRepository.findByProfileId(1L)).thenReturn(Optional.empty());

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_INCOMPLETE);
        assertThat(result.missingModules()).contains(
                OnboardingModuleCode.PERSONAL,
                OnboardingModuleCode.WORK,
                OnboardingModuleCode.BANK_CARD,
                OnboardingModuleCode.CONTACT,
                OnboardingModuleCode.DEVICE,
                OnboardingModuleCode.IDENTITY
        );
    }

    @Test
    void returnsSyncedWhenRequiredModulesCompleteEvenIfIdentityMissing() {
        when(profilePersonalRepository.findByProfileId(1L)).thenReturn(Optional.of(mock()));
        when(profileWorkRepository.findByProfileId(1L)).thenReturn(Optional.of(mock()));
        when(profileBankCardRepository.findByProfileId(1L)).thenReturn(Optional.of(mock()));
        when(profileContactRepository.findModuleByProfileId(1L)).thenReturn(Optional.of(mock()));
        when(profileDeviceRepository.findByProfileId(1L)).thenReturn(Optional.of(mock()));

        var result = facade.getProgress(1L, "U10001");

        assertThat(result.kycStatus()).isEqualTo(OnboardingProgressFacade.KYC_SYNCED);
        assertThat(result.completedModules()).containsExactlyInAnyOrder(
                OnboardingModuleCode.PERSONAL,
                OnboardingModuleCode.WORK,
                OnboardingModuleCode.BANK_CARD,
                OnboardingModuleCode.CONTACT,
                OnboardingModuleCode.DEVICE
        );
        assertThat(result.missingModules()).containsExactly(OnboardingModuleCode.IDENTITY);
    }
}
