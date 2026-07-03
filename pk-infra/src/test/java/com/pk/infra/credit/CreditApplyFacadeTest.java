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
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
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
    private CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;
    @Mock
    private CreditApplyProperties creditApplyProperties;
    @Mock
    private CreditApplyHandler creditApplyHandler;
    @Mock
    private CreditApplyOutboxPublisher creditApplyOutboxPublisher;
    @Mock
    private CreditStatusHistoryRepository creditStatusHistoryRepository;
    @Mock
    private CreditStatusPollHandler creditStatusPollHandler;

    private CreditApplyFacade facade;

    @BeforeEach
    void setUp() {
        facade = new CreditApplyFacade(
                onboardingProgressFacade,
                creditApplicationRepository,
                profileVersionRepository,
                creditLenderStatusQueryRepository,
                creditApplyProperties,
                creditApplyHandler,
                creditApplyOutboxPublisher,
                creditStatusHistoryRepository,
                creditStatusPollHandler
        );
    }

    @Test
    void returnsIdempotentResultForExistingRequestId() {
        when(creditApplicationRepository.findByRequestId("req-1")).thenReturn(OptionalRecord());

        CreditApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand("req-1"));

        assertThat(result.applyId()).isEqualTo("APPLY-1");
        assertThat(result.status()).isEqualTo(CreditApplyFacade.PUBLIC_PROCESSING);
        assertThat(result.creditApplyNo()).isEqualTo("CA-1");
        verify(creditApplicationRepository, never()).insert(any());
        verify(creditApplyHandler, never()).submit(any());
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

        assertThatThrownBy(() -> facade.apply(1L, "partner-1", "81234567890", sampleCommand("req-1")))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void submitsToLenderInlineWhenConfigured() {
        when(creditApplicationRepository.findByRequestId("req-1")).thenReturn(java.util.Optional.empty());
        when(onboardingProgressFacade.getProgress(1L, "partner-1")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-1",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("PERSONAL"),
                        List.of()
                )
        );
        when(profileVersionRepository.createSnapshot(1L, "81234567890", List.of("PERSONAL"), "CREDIT_APPLY")).thenReturn(9L);
        when(creditApplicationRepository.insert(any())).thenReturn(100L);
        when(creditApplyProperties.inlineEnabled()).thenReturn(true);
        when(creditApplyHandler.submit(any(CreditApplyJob.class))).thenReturn("CA-NEW");

        CreditApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand("req-1"));

        assertThat(result.applyId()).startsWith("APPLY");
        assertThat(result.status()).isEqualTo(CreditApplicationStatus.PROCESSING);
        assertThat(result.creditApplyNo()).isEqualTo("CA-NEW");
        verify(creditApplyHandler).submit(any(CreditApplyJob.class));
        verify(creditApplyOutboxPublisher, never()).publish(any());
    }

    @Test
    void syncsFromLenderWhenStatusIsNotTerminal() {
        CreditApplicationRepository.CreditApplicationRecord record = OptionalRecord().get();
        when(creditApplicationRepository.findLatestByProfileId(1L))
                .thenReturn(java.util.Optional.of(record));
        when(creditApplicationRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(java.util.Optional.of(record));

        CreditApplyFacade.StatusResult result = facade.getStatus(1L);

        assertThat(result.applyId()).isEqualTo("APPLY-1");
        assertThat(result.status()).isEqualTo(CreditApplicationStatus.PROCESSING);
        verify(creditStatusPollHandler).syncFromLenderForApi(record);
    }

    @Test
    void skipsLenderSyncWhenStatusIsTerminal() {
        CreditApplicationRepository.CreditApplicationRecord record = new CreditApplicationRepository.CreditApplicationRecord(
                1L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "partner-1",
                "81234567890",
                9L,
                "CA-1",
                CreditApplicationStatus.APPROVED,
                "SUCCESS",
                null
        );
        when(creditApplicationRepository.findLatestByProfileId(1L))
                .thenReturn(java.util.Optional.of(record));
        when(creditLenderStatusQueryRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(java.util.Optional.empty());

        CreditApplyFacade.StatusResult result = facade.getStatus(1L);

        assertThat(result.status()).isEqualTo(CreditApplicationStatus.APPROVED);
        verify(creditStatusPollHandler, never()).syncFromLenderForApi(any());
    }

    @Test
    void throwsWhenUserHasNoCreditApplication() {
        when(creditApplicationRepository.findLatestByProfileId(1L))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> facade.getStatus(1L))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.UPSTREAM_APPLICATION_NOT_FOUND);
    }

    @Test
    void enqueuesOutboxWhenNotInline() {
        when(creditApplicationRepository.findByRequestId("req-1")).thenReturn(java.util.Optional.empty());
        when(onboardingProgressFacade.getProgress(1L, "partner-1")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-1",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("PERSONAL"),
                        List.of()
                )
        );
        when(profileVersionRepository.createSnapshot(1L, "81234567890", List.of("PERSONAL"), "CREDIT_APPLY")).thenReturn(9L);
        when(creditApplicationRepository.insert(any())).thenReturn(100L);
        when(creditApplyProperties.inlineEnabled()).thenReturn(false);

        CreditApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand("req-1"));

        assertThat(result.applyId()).startsWith("APPLY");
        assertThat(result.status()).isEqualTo(CreditApplicationStatus.PROCESSING);
        assertThat(result.creditApplyNo()).isNull();
        verify(creditApplyOutboxPublisher).publish(any(CreditApplyJob.class));
        verify(creditApplyHandler, never()).submit(any());
    }

    private static java.util.Optional<CreditApplicationRepository.CreditApplicationRecord> OptionalRecord() {
        return java.util.Optional.of(new CreditApplicationRepository.CreditApplicationRecord(
                1L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "partner-1",
                "81234567890",
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
