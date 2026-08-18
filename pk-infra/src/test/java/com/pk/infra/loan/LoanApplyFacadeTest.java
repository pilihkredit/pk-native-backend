package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.LoanTrialQuoteDetail;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanLenderStatusQueryRepository;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanApplyFacadeTest {
    @Mock
    private OnboardingProgressFacade onboardingProgressFacade;
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private LoanQuoteRepository loanQuoteRepository;
    @Mock
    private ProfileVersionRepository profileVersionRepository;
    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanLenderStatusQueryRepository loanLenderStatusQueryRepository;
    @Mock
    private LoanStatusHistoryRepository loanStatusHistoryRepository;
    @Mock
    private LoanApplyHandler loanApplyHandler;
    @Mock
    private LoanApplyProperties loanApplyProperties;
    @Mock
    private LoanApplyOutboxPublisher loanApplyOutboxPublisher;
    @Mock
    private LoanStatusPollHandler loanStatusPollHandler;

    private LoanApplyFacade facade;

    @BeforeEach
    void setUp() {
        facade = new LoanApplyFacade(
                onboardingProgressFacade,
                creditApplicationRepository,
                loanQuoteRepository,
                profileVersionRepository,
                loanApplicationRepository,
                loanLenderStatusQueryRepository,
                loanStatusHistoryRepository,
                loanApplyHandler,
                loanApplyProperties,
                loanApplyOutboxPublisher,
                loanStatusPollHandler
        );
    }

    @Test
    void syncsFromLenderWhenStatusIsNotTerminal() {
        LoanApplicationRepository.LoanApplicationRecord record = existingLoan();
        when(loanApplicationRepository.findByLoanApplyIdAndUserId("QUOTE-1", 1L))
                .thenReturn(Optional.of(record));
        when(loanLenderStatusQueryRepository.findLatestByLoanApplyIdAndUserId("QUOTE-1", 1L))
                .thenReturn(Optional.empty());

        LoanApplyFacade.StatusResult result = facade.getStatus(1L, "QUOTE-1");

        assertThat(result.loanApplyId()).isEqualTo("QUOTE-1");
        assertThat(result.status()).isEqualTo(LoanApplicationStatus.PROCESSING);
        verify(loanStatusPollHandler).syncFromLenderForApi(record);
    }

    @Test
    void skipsLenderSyncWhenStatusIsTerminal() {
        LoanApplicationRepository.LoanApplicationRecord record = new LoanApplicationRepository.LoanApplicationRecord(
                200L,
                "QUOTE-1",
                "REQ-1",
                "APPLY-1",
                100L,
                10L,
                "QUOTE-1",
                1L,
                9L,
                "LN-1",
                "USR-1",
                "BN-1",
                LoanApplicationStatus.DISBURSED,
                "SUCCESS",
                new BigDecimal("1500000"),
                new BigDecimal("1455000"),
                Instant.parse("2026-07-01T00:00:00Z")
        );
        when(loanApplicationRepository.findByLoanApplyIdAndUserId("QUOTE-1", 1L))
                .thenReturn(Optional.of(record));
        when(loanLenderStatusQueryRepository.findLatestByLoanApplyIdAndUserId("QUOTE-1", 1L))
                .thenReturn(Optional.of(new LoanLenderStatusQueryRepository.LoanLenderStatusQueryData(
                        "QUOTE-1",
                        1L,
                        "USR-1",
                        "LN-1",
                        "SUCCESS",
                        "BN-1",
                        new BigDecimal("1500000"),
                        new BigDecimal("1455000"),
                        Instant.parse("2026-07-01T00:00:00Z"),
                        null,
                        99L,
                        null,
                        "APP",
                        Instant.parse("2026-07-01T00:00:00Z")
                )));

        LoanApplyFacade.StatusResult result = facade.getStatus(1L, "QUOTE-1");

        assertThat(result.status()).isEqualTo(LoanApplicationStatus.DISBURSED);
        verify(loanStatusPollHandler, never()).syncFromLenderForApi(any());
    }

    @Test
    void returnsIdempotentResultForExistingRequestId() {
        when(loanApplicationRepository.findByRequestId("REQ-1")).thenReturn(Optional.of(existingLoan()));

        LoanApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand());

        assertThat(result.loanApplyId()).isEqualTo("QUOTE-1");
        assertThat(result.status()).isEqualTo(LoanApplyFacade.PUBLIC_PROCESSING);
        verify(loanQuoteRepository, never()).findByQuoteNo(any());
        verify(loanApplyOutboxPublisher, never()).publish(any());
    }

    @Test
    void usesQuoteNoAsLoanApplyIdAndPersistsScalars() {
        when(loanApplicationRepository.findByRequestId("REQ-1")).thenReturn(Optional.empty());
        when(creditApplicationRepository.findByApplyIdAndUserId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedCredit("APPLY-1")));
        when(loanQuoteRepository.findByQuoteNo("QUOTE-1")).thenReturn(Optional.of(quoteRecord()));
        when(onboardingProgressFacade.getProgress(1L, "partner-1")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-1",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("PERSONAL"),
                        List.of()
                )
        );
        when(profileVersionRepository.createSnapshot(1L, "81234567890", List.of("PERSONAL"), "LOAN_APPLY"))
                .thenReturn(9L);
        when(loanApplicationRepository.insert(any())).thenReturn(200L);
        when(loanApplyProperties.inlineEnabled()).thenReturn(true);
        when(loanApplyHandler.submit(any(LoanApplyJob.class))).thenReturn("LN-NEW");

        LoanApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand());

        assertThat(result.loanApplyId()).isEqualTo("QUOTE-1");
        assertThat(result.loanApplyNo()).isEqualTo("LN-NEW");

        ArgumentCaptor<LoanApplicationRepository.LoanApplicationInsert> insertCaptor =
                ArgumentCaptor.forClass(LoanApplicationRepository.LoanApplicationInsert.class);
        verify(loanApplicationRepository).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().loanApplyId()).isEqualTo("QUOTE-1");
        assertThat(insertCaptor.getValue().quoteId()).isEqualTo(10L);
        assertThat(insertCaptor.getValue().quoteNo()).isEqualTo("QUOTE-1");
        assertThat(insertCaptor.getValue().productCode()).isEqualTo("PD001");
        assertThat(insertCaptor.getValue().repayMethod()).isEqualTo("RP001");
        assertThat(insertCaptor.getValue().applyAmt()).isEqualByComparingTo("1500000");

        ArgumentCaptor<LoanApplyJob> jobCaptor = ArgumentCaptor.forClass(LoanApplyJob.class);
        verify(loanApplyHandler).submit(jobCaptor.capture());
        assertThat(jobCaptor.getValue().loanApplyId()).isEqualTo("QUOTE-1");
    }

    @Test
    void storesNullQuoteIdWhenQuoteMissingButStillUsesQuoteNo() {
        when(loanApplicationRepository.findByRequestId("REQ-1")).thenReturn(Optional.empty());
        when(creditApplicationRepository.findByApplyIdAndUserId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedCredit("APPLY-1")));
        when(loanQuoteRepository.findByQuoteNo("QUOTE-1")).thenReturn(Optional.empty());
        when(onboardingProgressFacade.getProgress(1L, "partner-1")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-1",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("PERSONAL"),
                        List.of()
                )
        );
        when(profileVersionRepository.createSnapshot(1L, "81234567890", List.of("PERSONAL"), "LOAN_APPLY"))
                .thenReturn(9L);
        when(loanApplicationRepository.insert(any())).thenReturn(200L);
        when(loanApplyProperties.inlineEnabled()).thenReturn(false);

        LoanApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand());

        assertThat(result.loanApplyId()).isEqualTo("QUOTE-1");

        ArgumentCaptor<LoanApplicationRepository.LoanApplicationInsert> insertCaptor =
                ArgumentCaptor.forClass(LoanApplicationRepository.LoanApplicationInsert.class);
        verify(loanApplicationRepository).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().quoteId()).isNull();
        assertThat(insertCaptor.getValue().loanApplyId()).isEqualTo("QUOTE-1");
        verify(loanApplyOutboxPublisher).publish(any(LoanApplyJob.class));
    }

    @Test
    void derivesApplyScalarsFromQuoteWhenClientOmitsThem() {
        when(loanApplicationRepository.findByRequestId("REQ-1")).thenReturn(Optional.empty());
        when(creditApplicationRepository.findByApplyIdAndUserId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedCredit("APPLY-1")));
        when(loanQuoteRepository.findByQuoteNo("QUOTE-1")).thenReturn(Optional.of(quoteRecord()));
        when(onboardingProgressFacade.getProgress(1L, "partner-1")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-1",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("PERSONAL"),
                        List.of()
                )
        );
        when(profileVersionRepository.createSnapshot(1L, "81234567890", List.of("PERSONAL"), "LOAN_APPLY"))
                .thenReturn(9L);
        when(loanApplicationRepository.insert(any())).thenReturn(200L);
        when(loanApplyProperties.inlineEnabled()).thenReturn(false);

        LoanApplyFacade.ApplyCommand command = new LoanApplyFacade.ApplyCommand(
                "REQ-1",
                "APPLY-1",
                "QUOTE-1",
                null,
                null,
                null,
                null,
                "Modal Usaha",
                null,
                null,
                null,
                null,
                null,
                sampleCommand().device(),
                List.of()
        );

        LoanApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", command);

        assertThat(result.loanApplyId()).isEqualTo("QUOTE-1");
        ArgumentCaptor<LoanApplicationRepository.LoanApplicationInsert> insertCaptor =
                ArgumentCaptor.forClass(LoanApplicationRepository.LoanApplicationInsert.class);
        verify(loanApplicationRepository).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().applyAmt()).isEqualByComparingTo("1500000");
        assertThat(insertCaptor.getValue().productCode()).isEqualTo("PD001");
        assertThat(insertCaptor.getValue().repayMethod()).isEqualTo("RP001");
        verify(loanApplyOutboxPublisher).publish(any(LoanApplyJob.class));
    }

    private static LoanApplicationRepository.LoanApplicationRecord existingLoan() {
        return new LoanApplicationRepository.LoanApplicationRecord(
                200L,
                "QUOTE-1",
                "REQ-1",
                "APPLY-1",
                100L,
                10L,
                "QUOTE-1",
                1L,
                9L,
                "LN-1",
                "USR-1",
                null,
                LoanApplicationStatus.PROCESSING,
                "PROCESSING",
                new BigDecimal("1500000"),
                null,
                null
        );
    }

    private static LoanQuoteRepository.LoanQuoteRecord quoteRecord() {
        return new LoanQuoteRepository.LoanQuoteRecord(
                10L,
                "QUOTE-1",
                1L,
                100L,
                null,
                77L,
                new LoanTrialQuoteDetail(
                        "APPLY-1",
                        "CA-1",
                        "USR-1",
                        new BigDecimal("1500000"),
                        "PD001",
                        "RP001",
                        6,
                        new BigDecimal("1500000"),
                        new BigDecimal("1500000"),
                        new BigDecimal("1455000"),
                        null,
                        new BigDecimal("1600000"),
                        new BigDecimal("1600000"),
                        new BigDecimal("100000"),
                        null,
                        null,
                        30L,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                Instant.now()
        );
    }

    private static CreditApplicationRepository.CreditApplicationRecord approvedCredit(String applyId) {
        return new CreditApplicationRepository.CreditApplicationRecord(
                100L,
                applyId,
                "req-1",
                "apipartner",
                1L,
                "partner-1",
                "CA-1"
        );
    }

    private static LoanApplyFacade.ApplyCommand sampleCommand() {
        return new LoanApplyFacade.ApplyCommand(
                "REQ-1",
                "APPLY-1",
                "QUOTE-1",
                new BigDecimal("1500000"),
                "PD001",
                "RP001",
                null,
                "Modal Usaha",
                null,
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
