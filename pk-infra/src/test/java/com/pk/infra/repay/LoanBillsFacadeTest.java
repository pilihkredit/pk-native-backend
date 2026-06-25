package com.pk.infra.repay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pk.core.repay.RepayBillStatus;
import com.pk.core.repay.RepayTermStatus;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.core.repay.port.RepaymentPlanTermRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanBillsFacadeTest {
    @Mock
    private LoanBillReadRepository loanBillReadRepository;
    @Mock
    private RepaymentPlanTermRepository repaymentPlanTermRepository;

    private LoanBillsFacade facade;

    @BeforeEach
    void setUp() {
        facade = new LoanBillsFacade(loanBillReadRepository, repaymentPlanTermRepository);
    }

    @Test
    void listActiveBillsAggregatesNextDueAndDisplayFields() {
        when(loanBillReadRepository.findByProfileIdAndBillFilter(1L, LoanBillReadRepository.BillFilter.ACTIVE))
                .thenReturn(List.of(loanRecord()));
        when(repaymentPlanTermRepository.findByLoanApplicationId(100L))
                .thenReturn(List.of(
                        term(1, RepayTermStatus.UNPAID, Instant.parse("2025-06-13T00:00:00Z"), new BigDecimal("295000")),
                        term(2, RepayTermStatus.PAID, Instant.parse("2025-07-13T00:00:00Z"), new BigDecimal("295000"))
                ));

        LoanBillsFacade.BillsResult result = facade.listBills(1L, "ACTIVE");

        assertThat(result.bills()).hasSize(1);
        assertThat(result.bills().getFirst().billStatus()).isEqualTo(RepayBillStatus.NORMAL);
        assertThat(result.bills().getFirst().nextDueAmountDisplay()).isEqualTo("Rp 295.000");
        assertThat(result.bills().getFirst().applyAmtDisplay()).isEqualTo("Rp 1.500.000");
        assertThat(result.bills().getFirst().canReloan()).isFalse();
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

    private static RepaymentPlanTermRepository.TermRecord term(
            int termNo,
            String status,
            Instant dueDate,
            BigDecimal shouldAmount
    ) {
        return new RepaymentPlanTermRepository.TermRecord(
                termNo,
                100L,
                "LOAN-1",
                "BN-1",
                "SUB-" + termNo,
                termNo,
                status,
                dueDate,
                null,
                shouldAmount,
                shouldAmount,
                BigDecimal.ZERO,
                0,
                null,
                null,
                Instant.now()
        );
    }
}
