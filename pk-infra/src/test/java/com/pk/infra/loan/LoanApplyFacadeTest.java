package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
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
    private CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;
    @Mock
    private LoanQuoteRepository loanQuoteRepository;
    @Mock
    private LoanProductFacade loanProductFacade;
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
                creditLenderStatusQueryRepository,
                loanQuoteRepository,
                loanProductFacade,
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
        when(loanApplicationRepository.findByLoanApplyIdAndProfileId("LOAN-1", 1L))
                .thenReturn(Optional.of(record));
        when(loanLenderStatusQueryRepository.findByLoanApplyIdAndProfileId("LOAN-1", 1L))
                .thenReturn(Optional.empty());

        LoanApplyFacade.StatusResult result = facade.getStatus(1L, "LOAN-1");

        assertThat(result.loanApplyId()).isEqualTo("LOAN-1");
        assertThat(result.status()).isEqualTo(LoanApplicationStatus.PROCESSING);
        assertThat(result.applyStatus()).isEqualTo("PROCESSING");
        verify(loanStatusPollHandler).syncFromLenderForApi(record);
    }

    @Test
    void skipsLenderSyncWhenStatusIsTerminal() {
        LoanApplicationRepository.LoanApplicationRecord record = new LoanApplicationRepository.LoanApplicationRecord(
                200L,
                "LOAN-1",
                "REQ-1",
                "APPLY-1",
                "81234567890",
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
                Instant.parse("2026-07-01T00:00:00Z"),
                null,
                null
        );
        when(loanApplicationRepository.findByLoanApplyIdAndProfileId("LOAN-1", 1L))
                .thenReturn(Optional.of(record));
        when(loanLenderStatusQueryRepository.findByLoanApplyIdAndProfileId("LOAN-1", 1L))
                .thenReturn(Optional.of(new LoanLenderStatusQueryRepository.LoanLenderStatusQueryData(
                        "LOAN-1",
                        1L,
                        "81234567890",
                        "USR-1",
                        "LN-1",
                        "SUCCESS",
                        "BN-1",
                        new BigDecimal("1500000"),
                        new BigDecimal("1455000"),
                        Instant.parse("2026-07-01T00:00:00Z"),
                        null,
                        "{}",
                        "{}",
                        Instant.parse("2026-07-01T00:00:00Z")
                )));

        LoanApplyFacade.StatusResult result = facade.getStatus(1L, "LOAN-1");

        assertThat(result.status()).isEqualTo(LoanApplicationStatus.DISBURSED);
        assertThat(result.applyStatus()).isEqualTo("SUCCESS");
        verify(loanStatusPollHandler, never()).syncFromLenderForApi(any());
    }

    @Test
    void returnsIdempotentResultForExistingRequestId() {
        when(loanApplicationRepository.findByRequestId("REQ-1")).thenReturn(Optional.of(existingLoan()));

        LoanApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand());

        assertThat(result.loanApplyId()).isEqualTo("LOAN-1");
        assertThat(result.status()).isEqualTo(LoanApplyFacade.PUBLIC_PROCESSING);
        verify(loanQuoteRepository, never()).findByQuoteNo(any());
        verify(loanApplyOutboxPublisher, never()).publish(any());
    }

    @Test
    void submitsUsingFrontendParamsAndResolvesQuoteId() {
        when(loanApplicationRepository.findByRequestId("REQ-1")).thenReturn(Optional.empty());
        when(creditApplicationRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedCredit("APPLY-1")));
        when(creditLenderStatusQueryRepository.findLatestByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(limits()));
        when(loanProductFacade.resolveProductList(1L, "APPLY-1", true)).thenReturn(productList());
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

        assertThat(result.loanApplyId()).startsWith("LOAN");
        assertThat(result.status()).isEqualTo(LoanApplicationStatus.PROCESSING);
        assertThat(result.loanApplyNo()).isEqualTo("LN-NEW");

        ArgumentCaptor<LoanApplicationRepository.LoanApplicationInsert> insertCaptor =
                ArgumentCaptor.forClass(LoanApplicationRepository.LoanApplicationInsert.class);
        verify(loanApplicationRepository).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().quoteId()).isEqualTo(10L);
        assertThat(insertCaptor.getValue().quoteNo()).isEqualTo("QUOTE-1");
        assertThat(insertCaptor.getValue().applyAmt()).isEqualByComparingTo("1500000");

        ArgumentCaptor<LoanApplyJob> jobCaptor = ArgumentCaptor.forClass(LoanApplyJob.class);
        verify(loanApplyHandler).submit(jobCaptor.capture());
        assertThat(jobCaptor.getValue().productCode()).isEqualTo("PD001");
        assertThat(jobCaptor.getValue().repayMethod()).isEqualTo("RP001");
        assertThat(jobCaptor.getValue().applyAmt()).isEqualByComparingTo("1500000");
        verify(loanApplyOutboxPublisher, never()).publish(any());
    }

    @Test
    void storesQuoteNoWithoutQuoteIdWhenQuoteMissing() {
        when(loanApplicationRepository.findByRequestId("REQ-1")).thenReturn(Optional.empty());
        when(creditApplicationRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedCredit("APPLY-1")));
        when(creditLenderStatusQueryRepository.findLatestByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(limits()));
        when(loanProductFacade.resolveProductList(1L, "APPLY-1", true)).thenReturn(productList());
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

        assertThat(result.loanApplyId()).startsWith("LOAN");
        assertThat(result.loanApplyNo()).isNull();

        ArgumentCaptor<LoanApplicationRepository.LoanApplicationInsert> insertCaptor =
                ArgumentCaptor.forClass(LoanApplicationRepository.LoanApplicationInsert.class);
        verify(loanApplicationRepository).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().quoteId()).isNull();
        assertThat(insertCaptor.getValue().quoteNo()).isEqualTo("QUOTE-1");

        verify(loanApplyOutboxPublisher).publish(any(LoanApplyJob.class));
        verify(loanApplyHandler, never()).submit(any());
    }

    private static LoanApplicationRepository.LoanApplicationRecord existingLoan() {
        return new LoanApplicationRepository.LoanApplicationRecord(
                200L,
                "LOAN-1",
                "REQ-1",
                "APPLY-1",
                "81234567890",
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
                null,
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
                "81234567890",
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
                "pendanaan",
                1L,
                "partner-1",
                "81234567890",
                "CA-1"
        );
    }

    private static CreditLenderStatusQueryRepository.CreditLenderStatusQueryData limits() {
        return new CreditLenderStatusQueryRepository.CreditLenderStatusQueryData(
                "APPLY-1",
                1L,
                "81234567890",
                "partner-1",
                "USR-1",
                "CA-1",
                "SUCCESS",
                Instant.now().plusSeconds(3600).toEpochMilli(),
                null,
                new BigDecimal("500000"),
                new BigDecimal("5000000"),
                new BigDecimal("3000000"),
                new BigDecimal("2000000"),
                new BigDecimal("100000"),
                null,
                Instant.now()
        );
    }

    private static ProductListResolver.ResolvedProductList productList() {
        return new ProductListResolver.ResolvedProductList(
                55L,
                "APPROVED",
                "AVAILABLE",
                List.of(),
                Instant.now()
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
