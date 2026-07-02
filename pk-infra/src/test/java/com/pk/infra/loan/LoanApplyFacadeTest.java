package com.pk.infra.loan;

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
import com.pk.core.credit.port.ProfileVersionRepository;
import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanQuoteRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.OnboardingProgressFacade;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private LoanStatusHistoryRepository loanStatusHistoryRepository;
    @Mock
    private LoanApplyOutboxPublisher loanApplyOutboxPublisher;

    private LoanApplyFacade facade;
    private LoanQuoteProperties loanQuoteProperties;

    @BeforeEach
    void setUp() {
        loanQuoteProperties = new LoanQuoteProperties();
        loanQuoteProperties.setTtl(Duration.ofMinutes(15));
        facade = new LoanApplyFacade(
                onboardingProgressFacade,
                creditApplicationRepository,
                creditLenderStatusQueryRepository,
                loanQuoteRepository,
                loanQuoteProperties,
                loanProductFacade,
                profileVersionRepository,
                loanApplicationRepository,
                loanStatusHistoryRepository,
                loanApplyOutboxPublisher
        );
    }

    @Test
    void returnsIdempotentResultForExistingLoanApplyId() {
        when(loanApplicationRepository.findByLoanApplyId("LOAN-1")).thenReturn(Optional.of(existingLoan()));

        LoanApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand());

        assertThat(result.loanApplyId()).isEqualTo("LOAN-1");
        assertThat(result.status()).isEqualTo(LoanApplyFacade.PUBLIC_PROCESSING);
        verify(loanQuoteRepository, never()).findByQuoteNo(any());
        verify(loanApplyOutboxPublisher, never()).publish(any());
    }

    @Test
    void rejectsExpiredQuote() {
        when(loanApplicationRepository.findByLoanApplyId("LOAN-1")).thenReturn(Optional.empty());
        when(loanQuoteRepository.findByQuoteNo("QUOTE-1")).thenReturn(Optional.of(expiredQuote()));

        assertThatThrownBy(() -> facade.apply(1L, "partner-1", "81234567890", sampleCommand()))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).apiCode())
                .isEqualTo(ApiCode.QUOTE_SNAPSHOT_EXPIRED);
    }

    @Test
    void acceptsValidQuoteAndEnqueuesOutbox() {
        when(loanApplicationRepository.findByLoanApplyId("LOAN-1")).thenReturn(Optional.empty());
        when(loanQuoteRepository.findByQuoteNo("QUOTE-1")).thenReturn(Optional.of(freshQuote()));
        when(loanQuoteRepository.countTermsByQuoteId(10L)).thenReturn(2);
        when(creditApplicationRepository.findById(100L)).thenReturn(Optional.of(approvedCredit()));
        when(creditLenderStatusQueryRepository.findByApplyIdAndProfileId("APPLY-1", 1L)).thenReturn(Optional.of(limits()));
        when(loanProductFacade.resolveProductList(1L, "APPLY-1", true)).thenReturn(productSnapshot());
        when(onboardingProgressFacade.getProgress(1L, "partner-1")).thenReturn(
                new OnboardingProgressFacade.OnboardingProgressResult(
                        "partner-1",
                        OnboardingProgressFacade.KYC_SYNCED,
                        List.of("PERSONAL"),
                        List.of()
                )
        );
        when(profileVersionRepository.createSnapshot(1L, "81234567890", List.of("PERSONAL"), "LOAN_APPLY")).thenReturn(9L);
        when(loanApplicationRepository.insert(any())).thenReturn(200L);

        LoanApplyFacade.ApplyResult result = facade.apply(1L, "partner-1", "81234567890", sampleCommand());

        assertThat(result.loanApplyId()).isEqualTo("LOAN-1");
        assertThat(result.status()).isEqualTo(LoanApplicationStatus.PROCESSING);
        assertThat(result.loanApplyNo()).isNull();
        verify(loanApplyOutboxPublisher).publish(any(LoanApplyJob.class));
    }

    private static LoanApplicationRepository.LoanApplicationRecord existingLoan() {
        return new LoanApplicationRepository.LoanApplicationRecord(
                200L,
                "LOAN-1",
                100L,
                10L,
                1L,
                9L,
                "LN-1",
                null,
                LoanApplicationStatus.PROCESSING,
                "PROCESSING",
                new BigDecimal("1500000"),
                null,
                null
        );
    }

    private static LoanQuoteRepository.LoanQuoteRecord freshQuote() {
        return new LoanQuoteRepository.LoanQuoteRecord(
                10L,
                "QUOTE-1",
                100L,
                55L,
                "PD001",
                "RP001",
                new BigDecimal("1500000"),
                new BigDecimal("1500000"),
                new BigDecimal("1455000"),
                new BigDecimal("1600000"),
                new BigDecimal("100000"),
                30,
                null,
                "{}",
                Instant.now()
        );
    }

    private static LoanQuoteRepository.LoanQuoteRecord expiredQuote() {
        return new LoanQuoteRepository.LoanQuoteRecord(
                10L,
                "QUOTE-1",
                100L,
                55L,
                "PD001",
                "RP001",
                new BigDecimal("1500000"),
                new BigDecimal("1500000"),
                new BigDecimal("1455000"),
                new BigDecimal("1600000"),
                new BigDecimal("100000"),
                30,
                null,
                "{}",
                Instant.now().minus(Duration.ofHours(1))
        );
    }

    private static CreditApplicationRepository.CreditApplicationRecord approvedCredit() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                100L,
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
                "{}",
                "{}",
                Instant.now()
        );
    }

    private static ProductListResolver.ResolvedProductList productSnapshot() {
        return new ProductListResolver.ResolvedProductList(
                55L,
                "SNAP-1",
                "APPROVED",
                "AVAILABLE",
                List.of(),
                Instant.now()
        );
    }

    private static LoanApplyFacade.ApplyCommand sampleCommand() {
        return new LoanApplyFacade.ApplyCommand(
                "LOAN-1",
                "QUOTE-1",
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
