package com.pk.infra.credit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditApplyFacadeTest {
    @Mock
    private OnboardingProgressFacade onboardingProgressFacade;
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private ProfileVersionRepository profileVersionRepository;
    @Mock
    private CreditLimitSnapshotRepository creditLimitSnapshotRepository;
    @Mock
    private CreditApplyOutboxPublisher creditApplyOutboxPublisher;
    @Mock
    private CreditStatusHistoryRepository creditStatusHistoryRepository;

    private CreditApplyFacade facade;

    @BeforeEach
    void setUp() {
        facade = new CreditApplyFacade(
                onboardingProgressFacade,
                creditApplicationRepository,
                profileVersionRepository,
                creditLimitSnapshotRepository,
                creditApplyOutboxPublisher,
                creditStatusHistoryRepository
        );
    }

    @Test
    void returnsIdempotentResultForExistingRequestId() {
        when(creditApplicationRepository.findByRequestId("req-1")).thenReturn(OptionalRecord());

        CreditApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", sampleCommand("req-1"));

        assertThat(result.applyId()).isEqualTo("APPLY-1");
        assertThat(result.status()).isEqualTo(CreditApplyFacade.PUBLIC_PROCESSING);
        verify(creditApplicationRepository, never()).insert(any());
        verify(creditApplyOutboxPublisher, never()).publish(any());
    }

    @Test
    void rejectsWhenKycNotSynced() {
        when(creditApplicationRepository.findByRequestId("req-1")).thenReturn(java.util.Optional.empty());
        when(onboardingProgressFacade.getProgress(1L, "partner-1")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-1",
                        OnboardingProgressFacade.KYC_INCOMPLETE,
                        List.of(),
                        List.of("PERSONAL")
                )
        );

        assertThatThrownBy(() -> facade.apply(1L, "partner-1", sampleCommand("req-1")))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void acceptsSyncedProfileAndEnqueuesOutbox() {
        when(creditApplicationRepository.findByRequestId("req-1")).thenReturn(java.util.Optional.empty());
        when(onboardingProgressFacade.getProgress(1L, "partner-1")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-1",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("PERSONAL"),
                        List.of()
                )
        );
        when(profileVersionRepository.createSnapshot(1L, List.of("PERSONAL"), "CREDIT_APPLY")).thenReturn(9L);
        when(creditApplicationRepository.insert(any())).thenReturn(100L);

        CreditApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", sampleCommand("req-1"));

        assertThat(result.applyId()).startsWith("APPLY");
        assertThat(result.status()).isEqualTo(CreditApplicationStatus.PROCESSING);
        verify(creditApplyOutboxPublisher).publish(any(CreditApplyJob.class));
    }

    private static java.util.Optional<CreditApplicationRepository.CreditApplicationRecord> OptionalRecord() {
        return java.util.Optional.of(new CreditApplicationRepository.CreditApplicationRecord(
                1L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                9L,
                "CA-1",
                CreditApplicationStatus.PROCESSING,
                "PROCESSING",
                null
        ));
    }

    private static CreditApplyFacade.ApplyCommand sampleCommand(String requestId) {
        return new CreditApplyFacade.ApplyCommand(
                requestId,
                null,
                null,
                null,
                null,
                new LenderDeviceContext(
                        "LenderApp",
                        "1.0.0",
                        "com.example",
                        "device-1",
                        "android",
                        null,
                        java.util.Map.of(),
                        "ClientApp"
                ),
                List.of()
        );
    }
}
