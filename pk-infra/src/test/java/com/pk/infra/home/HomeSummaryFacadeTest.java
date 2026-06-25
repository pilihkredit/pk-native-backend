package com.pk.infra.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pk.core.home.HomeNextAction;
import com.pk.core.home.HomeUserStage;
import com.pk.core.home.port.HomeLifecycleReadRepository;
import com.pk.core.home.port.HomeLifecycleReadRepository.CreditApplySnapshot;
import com.pk.core.home.port.HomeLifecycleReadRepository.LoanApplySnapshot;
import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ProfileBankCardData;
import com.pk.core.profile.ProfileContactsModuleData;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.ProfilePersonalData;
import com.pk.core.profile.ProfileWorkData;
import com.pk.core.profile.port.ProfileContactRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.ProfileBankCardRepository;
import com.pk.core.profile.port.ProfilePersonalRepository;
import com.pk.core.profile.port.ProfileWorkRepository;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HomeSummaryFacadeTest {
    private ProfilePersonalRepository profilePersonalRepository;
    private ProfileWorkRepository profileWorkRepository;
    private ProfileBankCardRepository profileBankCardRepository;
    private ProfileContactRepository profileContactRepository;
    private ProfileDeviceRepository profileDeviceRepository;
    private HomeLifecycleReadRepository homeLifecycleReadRepository;
    private HomeSummaryFacade facade;

    @BeforeEach
    void setUp() {
        profilePersonalRepository = mock(ProfilePersonalRepository.class);
        profileWorkRepository = mock(ProfileWorkRepository.class);
        profileBankCardRepository = mock(ProfileBankCardRepository.class);
        profileContactRepository = mock(ProfileContactRepository.class);
        profileDeviceRepository = mock(ProfileDeviceRepository.class);
        homeLifecycleReadRepository = mock(HomeLifecycleReadRepository.class);
        facade = new HomeSummaryFacade(
                new OnboardingProgressFacade(
                        profilePersonalRepository,
                        profileWorkRepository,
                        profileBankCardRepository,
                        profileContactRepository,
                        profileDeviceRepository
                ),
                homeLifecycleReadRepository
        );
    }

    @Test
    void returnsOnboardingWhenKycIncomplete() {
        stubIncompleteOnboarding();

        var result = facade.getSummary(1L, "U10001");

        assertThat(result.userStage()).isEqualTo(HomeUserStage.ONBOARDING);
        assertThat(result.nextAction()).isEqualTo(HomeNextAction.COMPLETE_PROFILE);
        assertThat(result.latestCreditApply()).isNull();
        assertThat(result.pendingRepayBillCount()).isZero();
    }

    @Test
    void returnsCreditPendingWhenKycSyncedWithoutCreditApply() {
        stubSyncedOnboarding();
        when(homeLifecycleReadRepository.findLatestCreditApply(1L)).thenReturn(Optional.empty());
        when(homeLifecycleReadRepository.findLatestLoanApply(1L)).thenReturn(Optional.empty());
        when(homeLifecycleReadRepository.countPendingRepayLoans(1L)).thenReturn(0);
        when(homeLifecycleReadRepository.hasOverdueRepay(1L)).thenReturn(false);
        when(homeLifecycleReadRepository.hasDisbursedLoan(1L)).thenReturn(false);

        var result = facade.getSummary(1L, "U10001");

        assertThat(result.userStage()).isEqualTo(HomeUserStage.CREDIT_PENDING);
        assertThat(result.nextAction()).isEqualTo(HomeNextAction.APPLY_CREDIT);
    }

    @Test
    void returnsCreditApprovedWhenCreditApprovedAndNoLoanHistory() {
        stubSyncedOnboarding();
        when(homeLifecycleReadRepository.findLatestCreditApply(1L))
                .thenReturn(Optional.of(new CreditApplySnapshot("APPLY-1", "APPROVED")));
        when(homeLifecycleReadRepository.findLatestLoanApply(1L)).thenReturn(Optional.empty());
        when(homeLifecycleReadRepository.countPendingRepayLoans(1L)).thenReturn(0);
        when(homeLifecycleReadRepository.hasOverdueRepay(1L)).thenReturn(false);
        when(homeLifecycleReadRepository.hasDisbursedLoan(1L)).thenReturn(false);

        var result = facade.getSummary(1L, "U10001");

        assertThat(result.userStage()).isEqualTo(HomeUserStage.CREDIT_APPROVED);
        assertThat(result.nextAction()).isEqualTo(HomeNextAction.GO_LOAN);
        assertThat(result.latestCreditApply().applyId()).isEqualTo("APPLY-1");
    }

    @Test
    void returnsRepayWhenPendingBillsExist() {
        stubSyncedOnboarding();
        when(homeLifecycleReadRepository.findLatestCreditApply(1L))
                .thenReturn(Optional.of(new CreditApplySnapshot("APPLY-1", "APPROVED")));
        when(homeLifecycleReadRepository.findLatestLoanApply(1L))
                .thenReturn(Optional.of(new LoanApplySnapshot("LOAN-1", "DISBURSED")));
        when(homeLifecycleReadRepository.countPendingRepayLoans(1L)).thenReturn(2);
        when(homeLifecycleReadRepository.hasOverdueRepay(1L)).thenReturn(true);
        when(homeLifecycleReadRepository.hasDisbursedLoan(1L)).thenReturn(true);

        var result = facade.getSummary(1L, "U10001");

        assertThat(result.userStage()).isEqualTo(HomeUserStage.REPAY);
        assertThat(result.nextAction()).isEqualTo(HomeNextAction.VIEW_REPAY);
        assertThat(result.pendingRepayBillCount()).isEqualTo(2);
        assertThat(result.hasOverdue()).isTrue();
    }

    @Test
    void returnsReloanWhenCreditApprovedAndLoanAlreadyDisbursed() {
        stubSyncedOnboarding();
        when(homeLifecycleReadRepository.findLatestCreditApply(1L))
                .thenReturn(Optional.of(new CreditApplySnapshot("APPLY-1", "APPROVED")));
        when(homeLifecycleReadRepository.findLatestLoanApply(1L))
                .thenReturn(Optional.of(new LoanApplySnapshot("LOAN-1", "DISBURSED")));
        when(homeLifecycleReadRepository.countPendingRepayLoans(1L)).thenReturn(0);
        when(homeLifecycleReadRepository.hasOverdueRepay(1L)).thenReturn(false);
        when(homeLifecycleReadRepository.hasDisbursedLoan(1L)).thenReturn(true);

        var result = facade.getSummary(1L, "U10001");

        assertThat(result.userStage()).isEqualTo(HomeUserStage.RELOAN);
        assertThat(result.nextAction()).isEqualTo(HomeNextAction.GO_LOAN);
    }

    @Test
    void returnsLoanProcessingWhenLatestLoanIsProcessing() {
        stubSyncedOnboarding();
        when(homeLifecycleReadRepository.findLatestCreditApply(1L))
                .thenReturn(Optional.of(new CreditApplySnapshot("APPLY-1", "APPROVED")));
        when(homeLifecycleReadRepository.findLatestLoanApply(1L))
                .thenReturn(Optional.of(new LoanApplySnapshot("LOAN-1", "PROCESSING")));
        when(homeLifecycleReadRepository.countPendingRepayLoans(1L)).thenReturn(0);
        when(homeLifecycleReadRepository.hasOverdueRepay(1L)).thenReturn(false);
        when(homeLifecycleReadRepository.hasDisbursedLoan(1L)).thenReturn(false);

        var result = facade.getSummary(1L, "U10001");

        assertThat(result.userStage()).isEqualTo(HomeUserStage.LOAN_PROCESSING);
        assertThat(result.nextAction()).isEqualTo(HomeNextAction.WAIT);
    }

    private void stubIncompleteOnboarding() {
        when(profilePersonalRepository.findByProfileId(1L)).thenReturn(Optional.empty());
        when(profileWorkRepository.findByProfileId(1L)).thenReturn(Optional.empty());
        when(profileBankCardRepository.findByProfileId(1L)).thenReturn(Optional.empty());
        when(profileContactRepository.findModuleByProfileId(1L)).thenReturn(Optional.empty());
        when(profileDeviceRepository.findByProfileId(1L)).thenReturn(Optional.empty());
    }

    private void stubSyncedOnboarding() {
        EncryptedField encryptedField = new EncryptedField("cipher", new byte[12], new byte[16]);
        when(profilePersonalRepository.findByProfileId(1L)).thenReturn(Optional.of(
                new ProfilePersonalData(1L, "11", "1101", "110101", "addr", 1, encryptedField, null, "COMPLETED", "req-1")
        ));
        when(profileWorkRepository.findByProfileId(1L)).thenReturn(Optional.of(
                new ProfileWorkData(1L, 1, "Co", "11", "1101", "110101", "addr", "5000000", 1, 1, "COMPLETED", "req-1")
        ));
        when(profileBankCardRepository.findByProfileId(1L)).thenReturn(Optional.of(
                new ProfileBankCardData(1L, "BCA", encryptedField, "hash", "VERIFIED", null, "COMPLETED", "req-1")
        ));
        when(profileContactRepository.findModuleByProfileId(1L)).thenReturn(Optional.of(
                new ProfileContactsModuleData(1L, "COMPLETED", "req-1")
        ));
        when(profileDeviceRepository.findByProfileId(1L)).thenReturn(Optional.of(
                new ProfileDeviceData(1L, "device-1", "ANDROID", "app", "1.0", "com.pk", null, null, "req-1")
        ));
    }
}
