package com.pk.infra.home;

import com.pk.core.home.HomeNextAction;
import com.pk.core.home.HomeUserStage;
import com.pk.core.home.port.HomeLifecycleReadRepository;
import com.pk.core.home.port.HomeLifecycleReadRepository.CreditApplySnapshot;
import com.pk.core.home.port.HomeLifecycleReadRepository.LoanApplySnapshot;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.util.Optional;

public class HomeSummaryFacade {
    private static final String CREDIT_APPROVED = "APPROVED";
    private static final String CREDIT_PROCESSING = "PROCESSING";

    private final OnboardingProgressFacade onboardingProgressFacade;
    private final HomeLifecycleReadRepository homeLifecycleReadRepository;

    public HomeSummaryFacade(
            OnboardingProgressFacade onboardingProgressFacade,
            HomeLifecycleReadRepository homeLifecycleReadRepository
    ) {
        this.onboardingProgressFacade = onboardingProgressFacade;
        this.homeLifecycleReadRepository = homeLifecycleReadRepository;
    }

    public HomeSummaryResult getSummary(long profileId, String partnerUserId) {
        OnboardingProgressFacade.OnboardingProgressResult onboarding = onboardingProgressFacade.getProgress(
                profileId,
                partnerUserId
        );
        Optional<CreditApplySnapshot> latestCredit = homeLifecycleReadRepository.findLatestCreditApply(profileId);
        Optional<LoanApplySnapshot> latestLoan = homeLifecycleReadRepository.findLatestLoanApply(profileId);
        int pendingRepayBillCount = homeLifecycleReadRepository.countPendingRepayLoans(profileId);
        boolean hasOverdue = homeLifecycleReadRepository.hasOverdueRepay(profileId);
        boolean hasDisbursedLoan = homeLifecycleReadRepository.hasDisbursedLoan(profileId);

        StageDecision stageDecision = resolveStage(
                onboarding.kycStatus(),
                latestCredit.orElse(null),
                latestLoan.orElse(null),
                pendingRepayBillCount,
                hasDisbursedLoan
        );

        return new HomeSummaryResult(
                partnerUserId,
                stageDecision.userStage(),
                stageDecision.nextAction(),
                onboarding.kycStatus(),
                latestCredit.orElse(null),
                latestLoan.orElse(null),
                pendingRepayBillCount,
                hasOverdue
        );
    }

    private static StageDecision resolveStage(
            String kycStatus,
            CreditApplySnapshot latestCredit,
            LoanApplySnapshot latestLoan,
            int pendingRepayBillCount,
            boolean hasDisbursedLoan
    ) {
        if (OnboardingProgressFacade.KYC_INCOMPLETE.equals(kycStatus)) {
            return new StageDecision(HomeUserStage.ONBOARDING, HomeNextAction.COMPLETE_PROFILE);
        }
        if (pendingRepayBillCount > 0) {
            return new StageDecision(HomeUserStage.REPAY, HomeNextAction.VIEW_REPAY);
        }
        if (latestLoan != null && HomeUserStage.LOAN_ACTIVE_STATUS.equals(latestLoan.status())) {
            return new StageDecision(HomeUserStage.LOAN_PROCESSING, HomeNextAction.WAIT);
        }
        if (latestCredit != null && CREDIT_APPROVED.equals(latestCredit.status())) {
            String userStage = hasDisbursedLoan ? HomeUserStage.RELOAN : HomeUserStage.CREDIT_APPROVED;
            return new StageDecision(userStage, HomeNextAction.GO_LOAN);
        }
        if (latestCredit != null && CREDIT_PROCESSING.equals(latestCredit.status())) {
            return new StageDecision(HomeUserStage.CREDIT_PENDING, HomeNextAction.WAIT);
        }
        return new StageDecision(HomeUserStage.CREDIT_PENDING, HomeNextAction.APPLY_CREDIT);
    }

    private record StageDecision(String userStage, String nextAction) {
    }

    public record HomeSummaryResult(
            String partnerUserId,
            String userStage,
            String nextAction,
            String kycStatus,
            CreditApplySnapshot latestCreditApply,
            LoanApplySnapshot latestLoanApply,
            int pendingRepayBillCount,
            boolean hasOverdue
    ) {
    }
}
