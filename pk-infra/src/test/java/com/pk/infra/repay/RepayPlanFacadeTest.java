package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.repay.LenderRepayPlanTerm;
import com.pk.core.repay.RepayTermStatus;
import com.pk.core.repay.port.LenderRepayPlanPort;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
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
class RepayPlanFacadeTest {
    @Mock
    private LoanBillReadRepository loanBillReadRepository;
    @Mock
    private RepaymentPlanTermRepository repaymentPlanTermRepository;
    @Mock
    private LenderRepayPlanPort lenderRepayPlanPort;

    private RepayPlanFacade facade;

    @BeforeEach
    void setUp() {
        facade = new RepayPlanFacade(loanBillReadRepository, repaymentPlanTermRepository, lenderRepayPlanPort);
    }

    @Test
    void getPlanSyncsFromLenderAndReturnsDisplayFields() {
        when(loanBillReadRepository.findByProfileIdAndLoanApplyId(1L, "LOAN-1"))
                .thenReturn(Optional.of(loanRecord()));
        when(lenderRepayPlanPort.fetchPlan("LOAN-1")).thenReturn(lenderPlan());
        when(repaymentPlanTermRepository.findByLoanApplicationId(100L))
                .thenReturn(List.of(syncedTerm()));

        List<RepayPlanFacade.PlanResult> plans = facade.getPlan(1L, "LOAN-1");

        assertThat(plans).hasSize(1);
        assertThat(plans.getFirst().terms()).hasSize(1);
        assertThat(plans.getFirst().terms().getFirst().termNoDisplay()).isEqualTo("Cicilan ke-1");
        assertThat(plans.getFirst().terms().getFirst().shouldAmountDisplay()).isEqualTo("Rp 295.000");

        ArgumentCaptor<List<RepaymentPlanTermRepository.TermUpsert>> captor = ArgumentCaptor.forClass(List.class);
        verify(repaymentPlanTermRepository).upsertTerms(eq(100L), eq("LOAN-1"), eq("BN-1"), captor.capture(), any());
        assertThat(captor.getValue().getFirst().termStatus()).isEqualTo(RepayTermStatus.UNPAID);
    }

    private static LoanBillReadRepository.LoanBillRecord loanRecord() {
        return new LoanBillReadRepository.LoanBillRecord(
                100L,
                "LOAN-1",
                "LN-1",
                "BN-1",
                new BigDecimal("1500000"),
                "DISBURSED"
        );
    }

    private static LenderRepayPlanPort.LenderRepayPlanResult lenderPlan() {
        return new LenderRepayPlanPort.LenderRepayPlanResult(
                "LOAN-1",
                "LN-1",
                "BN-1",
                List.of(new LenderRepayPlanTerm(
                        1,
                        Instant.parse("2025-06-13T00:00:00Z"),
                        null,
                        null,
                        "SUB-1",
                        "NORMAL",
                        "N",
                        new BigDecimal("295000"),
                        new BigDecimal("295000"),
                        new BigDecimal("250000"),
                        new BigDecimal("45000"),
                        BigDecimal.ZERO,
                        0,
                        null,
                        "N",
                        "{\"shouldPrincipal\":250000,\"shouldInterest\":45000}"
                )),
                "{}"
        );
    }

    private static RepaymentPlanTermRepository.TermRecord syncedTerm() {
        return new RepaymentPlanTermRepository.TermRecord(
                1L,
                100L,
                "LOAN-1",
                "BN-1",
                "SUB-1",
                1,
                RepayTermStatus.UNPAID,
                Instant.parse("2025-06-13T00:00:00Z"),
                null,
                new BigDecimal("295000"),
                new BigDecimal("295000"),
                BigDecimal.ZERO,
                0,
                "{\"shouldPrincipal\":250000,\"shouldInterest\":45000}",
                null,
                Instant.now()
        );
    }
}
