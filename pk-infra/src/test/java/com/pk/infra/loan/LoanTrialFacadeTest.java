package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.port.LenderLoanTrialPort;
import com.pk.core.loan.port.LoanQuoteRepository;
import java.math.BigDecimal;
import java.time.Duration;
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
class LoanTrialFacadeTest {
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditLimitSnapshotRepository creditLimitSnapshotRepository;
    @Mock
    private LoanProductFacade loanProductFacade;
    @Mock
    private LenderLoanTrialPort lenderLoanTrialPort;
    @Mock
    private LoanQuoteRepository loanQuoteRepository;

    private LoanTrialFacade facade;

    @BeforeEach
    void setUp() {
        LoanQuoteProperties properties = new LoanQuoteProperties();
        properties.setTtl(Duration.ofMinutes(15));
        facade = new LoanTrialFacade(
                creditApplicationRepository,
                creditLimitSnapshotRepository,
                loanProductFacade,
                lenderLoanTrialPort,
                loanQuoteRepository,
                properties
        );
    }

    @Test
    void trialForceRefreshesProductsPersistsQuoteAndReturnsDisplayFields() {
        when(creditApplicationRepository.findByApplyIdAndProfileId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedRecord()));
        when(creditLimitSnapshotRepository.findByCreditApplicationId(100L))
                .thenReturn(Optional.of(limitSnapshot()));
        ProductListResolver.ResolvedProductList resolved = new ProductListResolver.ResolvedProductList(
                501L,
                "PSNAP-1",
                "APPROVED",
                "READY",
                List.of(),
                Instant.now()
        );
        when(loanProductFacade.resolveProductList(1L, "APPLY-1", true)).thenReturn(resolved);
        when(lenderLoanTrialPort.trial(any())).thenReturn(lenderTrialResult());
        when(loanQuoteRepository.insert(any(), any())).thenAnswer(invocation -> {
            LoanQuoteRepository.LoanQuoteInsert insert = invocation.getArgument(0);
            return new LoanQuoteRepository.LoanQuoteRecord(
                    900L,
                    insert.quoteNo(),
                    insert.creditApplicationId(),
                    insert.productSnapshotId(),
                    insert.productCode(),
                    insert.repayMethod(),
                    insert.applyAmt(),
                    insert.loanPrincipal(),
                    insert.payAmount(),
                    insert.schdAmount(),
                    insert.interest(),
                    insert.totalDays(),
                    insert.feeJson(),
                    insert.rawResponseJson(),
                    insert.quotedAt()
            );
        });

        LoanTrialFacade.TrialResult result = facade.trial(
                1L,
                new LoanTrialFacade.TrialCommand(
                        "REQ-1",
                        "APPLY-1",
                        new BigDecimal("1500000"),
                        "PD001",
                        "RP001",
                        null
                )
        );

        assertThat(result.quoteNo()).startsWith("QUOTE");
        assertThat(result.expiresAt()).isGreaterThan(Instant.now().toEpochMilli());
        assertThat(result.termInfo()).hasSize(1);
        assertThat(result.termInfo().getFirst().termNoDisplay()).isEqualTo("Cicilan ke-1");
        assertThat(result.termInfo().getFirst().schdAmountDisplay()).isEqualTo("Rp 295.000");

        verify(loanProductFacade).requireProductRepayMethod(resolved, "PD001", "RP001");
        ArgumentCaptor<LoanQuoteRepository.LoanQuoteInsert> insertCaptor =
                ArgumentCaptor.forClass(LoanQuoteRepository.LoanQuoteInsert.class);
        verify(loanQuoteRepository).insert(insertCaptor.capture(), any());
        assertThat(insertCaptor.getValue().productSnapshotId()).isEqualTo(501L);
        verify(lenderLoanTrialPort).trial(any(LenderLoanTrialPort.LenderLoanTrialCommand.class));
    }

    private static CreditApplicationRepository.CreditApplicationRecord approvedRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                100L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "81234567890",
                9L,
                "CA-1",
                CreditApplicationStatus.APPROVED,
                "SUCCESS",
                null
        );
    }

    private static CreditLimitSnapshotRepository.CreditLimitSnapshotData limitSnapshot() {
        return new CreditLimitSnapshotRepository.CreditLimitSnapshotData(
                100L,
                "81234567890",
                new BigDecimal("500000"),
                new BigDecimal("3000000"),
                new BigDecimal("2500000"),
                new BigDecimal("2800000"),
                new BigDecimal("100000"),
                Instant.parse("2026-06-30T00:00:00Z"),
                "CREDIT_CALLBACK"
        );
    }

    private static LenderLoanTrialPort.LenderLoanTrialResult lenderTrialResult() {
        return new LenderLoanTrialPort.LenderLoanTrialResult(
                new BigDecimal("1500000"),
                new BigDecimal("1455000"),
                new BigDecimal("1770000"),
                new BigDecimal("270000"),
                6,
                new BigDecimal("1455000"),
                180,
                List.of(new LenderTrialTerm(
                        1,
                        Instant.parse("2025-06-13T00:00:00Z"),
                        new BigDecimal("295000"),
                        new BigDecimal("250000"),
                        new BigDecimal("45000")
                )),
                "{\"loanTerm\":6}"
        );
    }
}
