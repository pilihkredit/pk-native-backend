package com.pk.infra.home;

import com.pk.core.home.HomeNextAction;
import com.pk.core.home.HomeUserStage;
import com.pk.core.home.port.LenderUserStatusPort;
import com.pk.core.home.port.UserLenderStatusQueryRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.time.Instant;

public class HomeSummaryFacade {
    private final OnboardingProgressFacade onboardingProgressFacade;
    private final LenderUserStatusPort lenderUserStatusPort;
    private final UserLenderStatusQueryRepository userLenderStatusQueryRepository;

    public HomeSummaryFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            LenderUserStatusPort lenderUserStatusPort,
            UserLenderStatusQueryRepository userLenderStatusQueryRepository
    ) {
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.lenderUserStatusPort = lenderUserStatusPort;
        this.userLenderStatusQueryRepository = userLenderStatusQueryRepository;
    }

    public HomeSummaryResult getSummary(
            long profileId,
            String partnerUserId,
            String mobileNo,
            LenderDeviceContext device
    ) {
        LenderUserStatusPort.LenderUserStatusResult lenderStatus = lenderUserStatusPort.queryStatus(
                new LenderUserStatusPort.LenderUserStatusCommand(partnerUserId, device)
        );
        Instant queriedAt = Instant.now();
        userLenderStatusQueryRepository.upsert(new UserLenderStatusQueryRepository.UserLenderStatusQueryData(
                profileId,
                mobileNo,
                lenderStatus.partnerUserId() == null ? partnerUserId : lenderStatus.partnerUserId(),
                lenderStatus.userId(),
                lenderStatus.userLoanLifeTimeStatus(),
                lenderStatus.userLoanLifeTimeLastAction(),
                lenderStatus.freezeEndTime(),
                lenderStatus.onLoanCount(),
                lenderStatus.creditContractExpireTime(),
                lenderStatus.autoCredit(),
                lenderStatus.requestJson(),
                lenderStatus.responseDataJson(),
                queriedAt
        ));
        return new HomeSummaryResult(
                lenderStatus.partnerUserId() == null ? partnerUserId : lenderStatus.partnerUserId(),
                lenderStatus.userId(),
                lenderStatus.userLoanLifeTimeStatus(),
                lenderStatus.userLoanLifeTimeLastAction(),
                lenderStatus.freezeEndTime(),
                lenderStatus.onLoanCount(),
                lenderStatus.creditContractExpireTime(),
                lenderStatus.autoCredit()
        );
    }

    public String resolveLocalUserStage(long profileId, String partnerUserId) {
        OnboardingProgressFacade.OnboardingProgressResult onboarding = onboardingProgressFacade.getProgress(
                profileId,
                partnerUserId
        );
        return resolveLocalStage(onboarding.kycStatus()).userStage();
    }

    private static StageDecision resolveLocalStage(String kycStatus) {
        if (OnboardingProgressFacade.KYC_INCOMPLETE.equals(kycStatus)) {
            return new StageDecision(HomeUserStage.ONBOARDING, HomeNextAction.COMPLETE_PROFILE);
        }
        return new StageDecision(HomeUserStage.READY, null);
    }

    private record StageDecision(String userStage, String nextAction) {
    }

    public record HomeSummaryResult(
            String partnerUserId,
            String userId,
            Integer userLoanLifeTimeStatus,
            Integer userLoanLifeTimeLastAction,
            Long freezeEndTime,
            Integer onLoanCount,
            Long creditContractExpireTime,
            Boolean autoCredit
    ) {
    }
}
