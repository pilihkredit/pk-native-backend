package com.pk.infra.profile;

import com.pk.core.onboarding.OnboardingModuleCode;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import java.util.ArrayList;
import java.util.List;

public class OnboardingProgressFacade {
    public static final String KYC_INCOMPLETE = "INCOMPLETE";
    public static final String KYC_SYNCED = "SYNCED";

    private final ProfilePersonalRepository profilePersonalRepository;
    private final ProfileBankCardRepository profileBankCardRepository;
    private final ProfileContactRepository profileContactRepository;
    private final ProfileDeviceRepository profileDeviceRepository;

    public OnboardingProgressFacade(
            ProfilePersonalRepository profilePersonalRepository,
            ProfileBankCardRepository profileBankCardRepository,
            ProfileContactRepository profileContactRepository,
            ProfileDeviceRepository profileDeviceRepository
    ) {
        this.profilePersonalRepository = profilePersonalRepository;
        this.profileBankCardRepository = profileBankCardRepository;
        this.profileContactRepository = profileContactRepository;
        this.profileDeviceRepository = profileDeviceRepository;
    }

    public OnboardingProgressResult getProgress(long profileId, String partnerUserId) {
        List<String> completedModules = new ArrayList<>();
        List<String> missingModules = new ArrayList<>();

        trackModule(
                OnboardingModuleCode.PERSONAL,
                profilePersonalRepository.findByProfileId(profileId).isPresent(),
                completedModules,
                missingModules
        );
        trackModule(
                OnboardingModuleCode.BANK_CARD,
                profileBankCardRepository.findByProfileId(profileId).isPresent(),
                completedModules,
                missingModules
        );
        trackModule(
                OnboardingModuleCode.CONTACT,
                profileContactRepository.findModuleByProfileId(profileId).isPresent(),
                completedModules,
                missingModules
        );
        trackModule(
                OnboardingModuleCode.DEVICE,
                profileDeviceRepository.existsByProfileId(profileId),
                completedModules,
                missingModules
        );
        missingModules.add(OnboardingModuleCode.IDENTITY);

        boolean allRequiredComplete = missingModules.stream()
                .noneMatch(module -> isRequiredForSync(module));
        String kycStatus = allRequiredComplete ? KYC_SYNCED : KYC_INCOMPLETE;

        return new OnboardingProgressResult(
                partnerUserId,
                kycStatus,
                List.copyOf(completedModules),
                List.copyOf(missingModules)
        );
    }

    private static void trackModule(
            String moduleCode,
            boolean completed,
            List<String> completedModules,
            List<String> missingModules
    ) {
        if (completed) {
            completedModules.add(moduleCode);
        } else {
            missingModules.add(moduleCode);
        }
    }

    private static boolean isRequiredForSync(String moduleCode) {
        return OnboardingModuleCode.PERSONAL.equals(moduleCode)
                || OnboardingModuleCode.BANK_CARD.equals(moduleCode)
                || OnboardingModuleCode.CONTACT.equals(moduleCode)
                || OnboardingModuleCode.DEVICE.equals(moduleCode);
    }

    public record OnboardingProgressResult(
            String partnerUserId,
            String kycStatus,
            List<String> completedModules,
            List<String> missingModules
    ) {
    }
}
