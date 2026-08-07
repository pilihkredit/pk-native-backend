package com.pk.infra.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLenderStatusQueryRepository;
import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
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
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanTrialFacadeTest {
    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditLenderStatusQueryRepository creditLenderStatusQueryRepository;
    @Mock
    private LoanProductFacade loanProductFacade;
    @Mock
    private LenderLoanTrialPort lenderLoanTrialPort;
    @Mock
    private LoanQuoteRepository loanQuoteRepository;
    @Captor
    private ArgumentCaptor<List<LoanQuoteRepository.LoanQuoteTermInsert>> termCaptor;

    private LoanTrialFacade facade;

    @BeforeEach
    void setUp() {
        LoanQuoteProperties properties = new LoanQuoteProperties();
        properties.setTtl(Duration.ofMinutes(15));
        facade = new LoanTrialFacade(
                creditApplicationRepository,
                creditLenderStatusQueryRepository,
                loanProductFacade,
                lenderLoanTrialPort,
                loanQuoteRepository,
                properties
        );
    }

    @Test
    void trialForceRefreshesProductsPersistsQuoteAndReturnsDisplayFields() {
        when(creditApplicationRepository.findByApplyIdAndUserId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedRecord()));
        when(creditLenderStatusQueryRepository.findLatestByApplyIdAndUserId("APPLY-1", 1L))
                .thenReturn(Optional.of(statusQuery()));
        ProductListResolver.ResolvedProductList resolved = new ProductListResolver.ResolvedProductList(
                501L,
                "APPROVED",
                "READY",
                List.of(),
                Instant.now()
        );
        when(loanProductFacade.resolveProductList(1L, "APPLY-1", true)).thenReturn(resolved);
        when(lenderLoanTrialPort.trial(any())).thenReturn(lenderTrialResult());
        when(loanQuoteRepository.upsert(any(), any())).thenAnswer(invocation -> {
            LoanQuoteRepository.LoanQuoteInsert insert = invocation.getArgument(0);
            return new LoanQuoteRepository.LoanQuoteRecord(
                    900L,
                    insert.quoteNo(),
                    insert.userId(),
                    insert.creditApplicationId(),
                    insert.couponId(),
                    insert.externalInteractionId(),
                    insert.quote(),
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
                        88L
                )
        );

        assertThat(result.quoteNo()).startsWith("QUOTE");
        assertThat(result.expiresAt()).isGreaterThan(Instant.now().toEpochMilli());
        assertThat(result.quote().loanPrincipal()).isEqualByComparingTo("1455000");
        assertThat(result.quote().totalDays()).isEqualTo(180L);
        assertThat(result.termInfo()).hasSize(1);
        assertThat(result.termInfo().getFirst().termNoDisplay()).isEqualTo("Cicilan ke-1");
        assertThat(result.termInfo().getFirst().schdAmountDisplay()).isEqualTo("Rp 295.000");

        verify(loanProductFacade).requireProductRepayMethod(resolved, "PD001", "RP001");
        ArgumentCaptor<LoanQuoteRepository.LoanQuoteInsert> insertCaptor =
                ArgumentCaptor.forClass(LoanQuoteRepository.LoanQuoteInsert.class);
        verify(loanQuoteRepository).upsert(insertCaptor.capture(), any());
        assertThat(insertCaptor.getValue().userId()).isEqualTo(1L);
        assertThat(insertCaptor.getValue().couponId()).isEqualTo(88L);
        assertThat(insertCaptor.getValue().externalInteractionId()).isEqualTo(77L);
        assertThat(insertCaptor.getValue().quote().loanTerm()).isEqualTo(6);
        assertThat(insertCaptor.getValue().quote().handFee()).isEqualByComparingTo("45000");
        verify(loanQuoteRepository).upsert(any(), termCaptor.capture());
        assertThat(termCaptor.getValue()).allSatisfy(term -> {
            assertThat(term.term().shouldAmount()).isEqualByComparingTo("295000");
            assertThat(term.term().valueDate()).isEqualTo(1747180800000L);
        });
        ArgumentCaptor<LenderLoanTrialPort.LenderLoanTrialCommand> commandCaptor =
                ArgumentCaptor.forClass(LenderLoanTrialPort.LenderLoanTrialCommand.class);
        verify(lenderLoanTrialPort).trial(commandCaptor.capture());
        assertThat(commandCaptor.getValue().couponId()).isEqualTo(88L);
    }

    @Test
    void trialContinuesToLenderWhenCreditLimitSnapshotIsMissing() {
        when(creditApplicationRepository.findByApplyIdAndUserId("APPLY-1", 1L))
                .thenReturn(Optional.of(approvedRecord()));
        when(creditLenderStatusQueryRepository.findLatestByApplyIdAndUserId("APPLY-1", 1L))
                .thenReturn(Optional.empty());
        ProductListResolver.ResolvedProductList resolved = new ProductListResolver.ResolvedProductList(
                501L,
                "APPROVED",
                "READY",
                List.of(),
                Instant.now()
        );
        when(loanProductFacade.resolveProductList(1L, "APPLY-1", true)).thenReturn(resolved);
        when(lenderLoanTrialPort.trial(any())).thenReturn(lenderTrialResult());

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

        assertThat(result.quote()).isNotNull();
        verify(lenderLoanTrialPort).trial(any());
    }

    private static CreditApplicationRepository.CreditApplicationRecord approvedRecord() {
        return new CreditApplicationRepository.CreditApplicationRecord(
                100L,
                "APPLY-1",
                "req-1",
                "pendanaan",
                1L,
                "partner-1",
                "CA-1"
        );
    }

    private static CreditLenderStatusQueryRepository.CreditLenderStatusQueryData statusQuery() {
        return new CreditLenderStatusQueryRepository.CreditLenderStatusQueryData(
                "APPLY-1",
                1L,
                "partner-1",
                "USR-1",
                "CA-1",
                "SUCCESS",
                Instant.parse("2026-06-30T00:00:00Z").toEpochMilli(),
                null,
                new BigDecimal("500000"),
                new BigDecimal("3000000"),
                new BigDecimal("2500000"),
                new BigDecimal("2800000"),
                new BigDecimal("100000"),
                null,
                null,
                "APP",
                Instant.parse("2026-06-01T00:00:00Z")
        );
    }

    private static LenderLoanTrialPort.LenderLoanTrialResult lenderTrialResult() {
        LoanTrialQuoteDetail quote = new LoanTrialQuoteDetail(
                "APPLY-1",
                "CA-1",
                "USR-1",
                new BigDecimal("1500000"),
                "PD001",
                "RP001",
                6,
                new BigDecimal("1455000"),
                new BigDecimal("1455000"),
                new BigDecimal("1455000"),
                new BigDecimal("45000"),
                new BigDecimal("1770000"),
                new BigDecimal("1770000"),
                new BigDecimal("270000"),
                new BigDecimal("0.003"),
                new BigDecimal("0.003"),
                180L,
                "Admin Fee",
                BigDecimal.ZERO,
                "Service Fee",
                BigDecimal.ZERO,
                "Insurance Fee",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "0.5",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("1500000"),
                new BigDecimal("270000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "N",
                0,
                1747200000000L,
                1749792000000L,
                1757472000000L,
                false
        );
        return new LenderLoanTrialPort.LenderLoanTrialResult(
                quote,
                List.of(new LenderTrialTerm(
                        1,
                        1747180800000L,
                        1749772800000L,
                        1750377600000L,
                        new BigDecimal("295000"),
                        new BigDecimal("250000"),
                        new BigDecimal("250000"),
                        new BigDecimal("45000"),
                        new BigDecimal("45000"),
                        new BigDecimal("295000"),
                        new BigDecimal("250000"),
                        new BigDecimal("45000"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )),
                77L
        );
    }
}
